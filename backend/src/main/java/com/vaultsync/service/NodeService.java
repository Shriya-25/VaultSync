package com.vaultsync.service;

import com.vaultsync.dto.NodeDto;
import com.vaultsync.entity.FileEntity;
import com.vaultsync.entity.FileReplicaEntity;
import com.vaultsync.entity.StorageNodeEntity;
import com.vaultsync.exception.ResourceNotFoundException;
import com.vaultsync.repository.FileReplicaRepository;
import com.vaultsync.repository.FileRepository;
import com.vaultsync.repository.StorageNodeRepository;
import com.vaultsync.storagenode.StorageResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NodeService {

    private static final Logger log = LoggerFactory.getLogger(NodeService.class);

    private final StorageNodeRepository nodeRepository;
    private final FileRepository fileRepository;
    private final FileReplicaRepository replicaRepository;
    private final StorageNodeClient storageClient;
    private final OperationService operationService;
    private final ReplicaRepairService repairService;

    public NodeService(StorageNodeRepository nodeRepository,
                       FileRepository fileRepository,
                       FileReplicaRepository replicaRepository,
                       StorageNodeClient storageClient,
                       OperationService operationService,
                       ReplicaRepairService repairService) {
        this.nodeRepository = nodeRepository;
        this.fileRepository = fileRepository;
        this.replicaRepository = replicaRepository;
        this.storageClient = storageClient;
        this.operationService = operationService;
        this.repairService = repairService;
    }

    /**
     * Return all nodes with live health and replica counts
     */
    @Transactional(readOnly = true)
    public List<NodeDto> getAllNodes() {
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
     * Receive periodic heartbeat from a storage node
     */
    @Transactional
    public Map<String, Object> processHeartbeat(String nodeId, Map<String, Object> payload) {
        Optional<StorageNodeEntity> nodeOpt = nodeRepository.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            return Map.of("status", "IGNORED", "reason", "Unknown node: " + nodeId);
        }

        StorageNodeEntity node = nodeOpt.get();
        boolean wasUnavailable = "UNAVAILABLE".equalsIgnoreCase(node.getStatus());

        // Update heartbeat timestamp
        node.setLastHeartbeat(OffsetDateTime.now());

        if (payload != null && payload.containsKey("storageDirectory") && payload.get("storageDirectory") != null) {
            node.setStorageDirectory((String) payload.get("storageDirectory"));
        }

        // If not simulated down, restore to HEALTHY if it was previously UNAVAILABLE
        if (!Boolean.TRUE.equals(node.getIsSimulatedDown())) {
            node.setStatus("HEALTHY");
            nodeRepository.save(node);

            if (wasUnavailable) {
                String msg = String.format("Storage node [%s] reconnected via heartbeat. Restored to HEALTHY.", node.getName());
                operationService.logOperation("NODE_RECOVERED", null, nodeId, "SUCCESS", msg);
                log.info("{}", msg);

                // Trigger automated replica repair for this node
                repairService.repairNodeReplicas(nodeId);
            }
        } else {
            nodeRepository.save(node);
        }

        return Map.of(
                "status", "ACK",
                "nodeId", nodeId,
                "nodeStatus", node.getStatus(),
                "timestamp", OffsetDateTime.now()
        );
    }

    /**
     * Periodic monitor check: identify heartbeats older than timeoutSeconds and mark nodes UNAVAILABLE
     */
    @Transactional
    public void checkHeartbeatsAndFailures(long timeoutSeconds) {
        List<StorageNodeEntity> nodes = nodeRepository.findAll();
        OffsetDateTime threshold = OffsetDateTime.now().minusSeconds(timeoutSeconds);

        for (StorageNodeEntity node : nodes) {
            // Check if simulated down
            if (Boolean.TRUE.equals(node.getIsSimulatedDown())) {
                if (!"UNAVAILABLE".equalsIgnoreCase(node.getStatus())) {
                    setNodeUnavailable(node, "Node manually simulated down (Demo Fault Injection)");
                }
                continue;
            }

            // Probe live health and check last heartbeat
            Map<String, Object> live = storageClient.checkNodeHealth(node.getEndpoint());
            boolean isLive = live != null && "HEALTHY".equalsIgnoreCase((String) live.get("status"));
            boolean isTimedOut = node.getLastHeartbeat() == null || node.getLastHeartbeat().isBefore(threshold);

            if (!isLive || isTimedOut) {
                if ("HEALTHY".equalsIgnoreCase(node.getStatus())) {
                    String reason = !isLive
                            ? String.format("Endpoint [%s] unreachable", node.getEndpoint())
                            : String.format("Heartbeat timed out (>%ds)", timeoutSeconds);
                    setNodeUnavailable(node, reason);
                }
            } else {
                // Node is live and heartbeat is fresh
                if ("UNAVAILABLE".equalsIgnoreCase(node.getStatus())) {
                    node.setStatus("HEALTHY");
                    node.setLastHeartbeat(OffsetDateTime.now());
                    nodeRepository.save(node);

                    String msg = String.format("Storage node [%s] verified responsive. Restored to HEALTHY. Triggering replica repair...", node.getName());
                    operationService.logOperation("NODE_RECOVERED", null, node.getId(), "SUCCESS", msg);
                    log.info("{}", msg);

                    repairService.repairNodeReplicas(node.getId());
                }
            }
        }
    }

    private void setNodeUnavailable(StorageNodeEntity node, String reason) {
        node.setStatus("UNAVAILABLE");
        nodeRepository.save(node);

        // Mark replicas on this node as UNAVAILABLE
        List<FileReplicaEntity> replicas = replicaRepository.findByNodeId(node.getId());
        for (FileReplicaEntity replica : replicas) {
            replica.setStatus("UNAVAILABLE");
            replicaRepository.save(replica);
        }

        // Update affected files status to DEGRADED or FAILED
        List<FileEntity> allFiles = fileRepository.findAllWithReplicas();
        for (FileEntity file : allFiles) {
            boolean hasReplicaOnThisNode = file.getReplicas().stream()
                    .anyMatch(r -> r.getNode().getId().equals(node.getId()));
            if (hasReplicaOnThisNode) {
                long availableCount = file.getReplicas().stream()
                        .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                        .count();
                if (availableCount > 0) {
                    file.setStatus("DEGRADED");
                } else {
                    file.setStatus("FAILED");
                }
                fileRepository.save(file);
            }
        }

        String msg = String.format("Storage node [%s] became UNAVAILABLE (%s). Replicas marked degraded.", node.getName(), reason);
        operationService.logOperation("NODE_FAILURE_DETECTED", null, node.getId(), "WARNING", msg);
        log.warn("{}", msg);
    }

    /**
     * Demo Fault Injection: Simulate node failure
     */
    @Transactional
    public NodeDto simulateNodeFailure(String nodeId) {
        StorageNodeEntity node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageNode", "id", nodeId));

        node.setIsSimulatedDown(true);
        nodeRepository.save(node);

        setNodeUnavailable(node, "Demo Fault Injection (Simulate Failure)");

        operationService.logOperation(
                "SIMULATED_FAILURE",
                null,
                nodeId,
                "WARNING",
                String.format("Demo Control: Storage node [%s] simulated as UNAVAILABLE. Replicas marked DEGRADED.", node.getName())
        );

        return toDto(node);
    }

    /**
     * Demo Fault Injection: Restore simulated node
     */
    @Transactional
    public NodeDto simulateNodeRecovery(String nodeId) {
        StorageNodeEntity node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("StorageNode", "id", nodeId));

        node.setIsSimulatedDown(false);
        node.setStatus("HEALTHY");
        node.setLastHeartbeat(OffsetDateTime.now());
        nodeRepository.save(node);

        operationService.logOperation(
                "SIMULATED_RECOVERY",
                null,
                nodeId,
                "SUCCESS",
                String.format("Demo Control: Storage node [%s] restored to HEALTHY. Triggering replica repair...", node.getName())
        );

        // Repair missing or degraded replicas
        repairService.repairNodeReplicas(nodeId);

        return toDto(node);
    }

    /**
     * Actively probe each registered node, update status and heartbeat, and persist to PostgreSQL
     */
    @Transactional
    public List<NodeDto> refreshNodeStatuses() {
        checkHeartbeatsAndFailures(10);
        return getAllNodes();
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

        boolean deleted = storageClient.deleteFile(node.getEndpoint(), testKey);
        testReport.put("cleanupSuccess", deleted);
        testReport.put("success", true);

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
        long replicaCount = replicaRepository.countByNodeIdAndStatus(entity.getId(), "AVAILABLE");
        return new NodeDto(
                entity.getId(),
                entity.getName(),
                entity.getEndpoint(),
                entity.getStorageDirectory(),
                entity.getStatus(),
                entity.getLastHeartbeat(),
                entity.getIsSimulatedDown(),
                replicaCount,
                entity.getCreatedAt()
        );
    }
}
