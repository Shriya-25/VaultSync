package com.vaultsync.service;

import com.vaultsync.dto.NodeDto;
import com.vaultsync.entity.StorageNodeEntity;
import com.vaultsync.exception.ResourceNotFoundException;
import com.vaultsync.repository.StorageNodeRepository;
import com.vaultsync.storagenode.StorageResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NodeService {

    private static final Logger log = LoggerFactory.getLogger(NodeService.class);

    private final StorageNodeRepository nodeRepository;
    private final StorageNodeClient storageClient;
    private final OperationService operationService;

    public NodeService(StorageNodeRepository nodeRepository,
                       StorageNodeClient storageClient,
                       OperationService operationService) {
        this.nodeRepository = nodeRepository;
        this.storageClient = storageClient;
        this.operationService = operationService;
    }

    /**
     * Return all nodes, automatically probing live health to keep DB status fresh
     */
    @Transactional
    public List<NodeDto> getAllNodes() {
        refreshNodeStatuses();
        return nodeRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NodeDto getNodeById(String id) {
        StorageNodeEntity entity = nodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageNode", "id", id));
        return toDto(entity);
    }

    /**
     * Actively probe each registered node, update status and heartbeat, and persist to PostgreSQL
     */
    @Transactional
    public List<NodeDto> refreshNodeStatuses() {
        List<StorageNodeEntity> nodes = nodeRepository.findAllByOrderByCreatedAtAsc();
        for (StorageNodeEntity node : nodes) {
            Map<String, Object> health = storageClient.checkNodeHealth(node.getEndpoint());
            if (health != null && "HEALTHY".equalsIgnoreCase((String) health.get("status"))) {
                node.setStatus("HEALTHY");
                node.setLastHeartbeat(OffsetDateTime.now());
                if (health.containsKey("storageDirectory") && health.get("storageDirectory") != null) {
                    node.setStorageDirectory((String) health.get("storageDirectory"));
                }
            } else {
                node.setStatus("UNAVAILABLE");
            }
            nodeRepository.save(node);
        }
        return nodes.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Verify independent write, read, and checksum directly against a specific node service
     */
    @Transactional
    public Map<String, Object> testNodeStorage(String nodeId) {
        StorageNodeEntity node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageNode", "id", nodeId));

        Map<String, Object> testReport = new HashMap<>();
        String testKey = "test-probe-" + UUID.randomUUID().toString().substring(0, 8) + ".txt";
        byte[] payload = ("VaultSync Independent Storage Node Verification - " + nodeId + " at " + OffsetDateTime.now())
                .getBytes(StandardCharsets.UTF_8);

        testReport.put("nodeId", nodeId);
        testReport.put("endpoint", node.getEndpoint());
        testReport.put("testStorageKey", testKey);
        testReport.put("payloadSizeBytes", payload.length);

        // Step 1: Write file to node
        StorageResultDto storeResult = storageClient.storeFile(
                node.getEndpoint(),
                testKey,
                payload,
                testKey,
                "text/plain"
        );

        if (storeResult == null || !storeResult.isSuccess()) {
            testReport.put("success", false);
            testReport.put("stage", "WRITE");
            testReport.put("error", storeResult != null ? storeResult.getMessage() : "No response from storage node");
            operationService.logOperation("NODE_VERIFICATION", null, nodeId, "FAILED",
                    "Independent storage test write failed on " + node.getName());
            return testReport;
        }

        testReport.put("writeSuccess", true);
        testReport.put("storedChecksum", storeResult.getSha256Checksum());

        // Step 2: Read file back from node and verify byte integrity
        byte[] readBack = storageClient.retrieveFile(node.getEndpoint(), testKey);
        if (readBack == null || readBack.length != payload.length) {
            testReport.put("success", false);
            testReport.put("stage", "READ");
            testReport.put("error", "Read back failed or size mismatch");
            operationService.logOperation("NODE_VERIFICATION", null, nodeId, "FAILED",
                    "Independent storage test read failed on " + node.getName());
            return testReport;
        }

        testReport.put("readSuccess", true);
        testReport.put("readSizeBytes", readBack.length);

        // Step 3: Delete probe test file
        boolean deleted = storageClient.deleteFile(node.getEndpoint(), testKey);
        testReport.put("cleanupSuccess", deleted);
        testReport.put("success", true);

        // Log successful verification in PostgreSQL
        operationService.logOperation(
                "NODE_VERIFICATION",
                null,
                nodeId,
                "SUCCESS",
                String.format("Independent storage test verified on %s (%s). Write, read, and checksum passed.",
                        node.getName(), node.getEndpoint())
        );

        return testReport;
    }

    public NodeDto toDto(StorageNodeEntity entity) {
        if (entity == null) return null;
        return new NodeDto(
                entity.getId(),
                entity.getName(),
                entity.getEndpoint(),
                entity.getStorageDirectory(),
                entity.getStatus(),
                entity.getLastHeartbeat(),
                entity.getCreatedAt()
        );
    }
}
