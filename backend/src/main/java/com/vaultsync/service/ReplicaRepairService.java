package com.vaultsync.service;

import com.vaultsync.entity.FileEntity;
import com.vaultsync.entity.FileReplicaEntity;
import com.vaultsync.entity.StorageNodeEntity;
import com.vaultsync.repository.FileReplicaRepository;
import com.vaultsync.repository.FileRepository;
import com.vaultsync.repository.StorageNodeRepository;
import com.vaultsync.storagenode.StorageResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReplicaRepairService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaRepairService.class);

    private final FileRepository fileRepository;
    private final FileReplicaRepository replicaRepository;
    private final StorageNodeRepository nodeRepository;
    private final StorageNodeClient storageClient;
    private final OperationService operationService;

    // Concurrency guard: prevents concurrent repairs on the same file
    private final ConcurrentHashMap<String, Boolean> activeRepairFiles = new ConcurrentHashMap<>();

    public ReplicaRepairService(FileRepository fileRepository,
                                FileReplicaRepository replicaRepository,
                                StorageNodeRepository nodeRepository,
                                StorageNodeClient storageClient,
                                OperationService operationService) {
        this.fileRepository = fileRepository;
        this.replicaRepository = replicaRepository;
        this.nodeRepository = nodeRepository;
        this.storageClient = storageClient;
        this.operationService = operationService;
    }

    /**
     * Repair missing or degraded replicas on a specific recovered storage node
     */
    @Transactional
    public int repairNodeReplicas(String targetNodeId) {
        Optional<StorageNodeEntity> targetOpt = nodeRepository.findById(targetNodeId);
        if (targetOpt.isEmpty()) {
            return 0;
        }

        StorageNodeEntity targetNode = targetOpt.get();
        if (!"HEALTHY".equalsIgnoreCase(targetNode.getStatus()) || Boolean.TRUE.equals(targetNode.getIsSimulatedDown())) {
            log.debug("Target node [{}] is not healthy or is simulated down; skipping repair", targetNodeId);
            return 0;
        }

        List<FileEntity> allFiles = fileRepository.findAllWithReplicas();
        int repairedCount = 0;

        for (FileEntity file : allFiles) {
            // Check if file needs repair on targetNode
            FileReplicaEntity targetReplica = file.getReplicas().stream()
                    .filter(r -> r.getNode().getId().equals(targetNodeId))
                    .findFirst()
                    .orElse(null);

            boolean needsRepair = (targetReplica == null) || !"AVAILABLE".equalsIgnoreCase(targetReplica.getStatus());
            if (!needsRepair) {
                continue;
            }

            // Acquire lock to avoid concurrent repair
            if (activeRepairFiles.putIfAbsent(file.getId(), Boolean.TRUE) != null) {
                log.debug("Repair already in progress for file [{}]; skipping", file.getId());
                continue;
            }

            try {
                boolean success = repairSingleFileReplica(file, targetNode, targetReplica);
                if (success) {
                    repairedCount++;
                }
            } finally {
                activeRepairFiles.remove(file.getId());
            }
        }

        return repairedCount;
    }

    /**
     * Scan and repair all degraded files across healthy nodes
     */
    @Transactional
    public int repairAllDegradedFiles() {
        List<StorageNodeEntity> healthyNodes = nodeRepository.findAll().stream()
                .filter(n -> "HEALTHY".equalsIgnoreCase(n.getStatus()) && !Boolean.TRUE.equals(n.getIsSimulatedDown()))
                .toList();

        if (healthyNodes.size() < 2) {
            log.debug("Less than 2 healthy nodes available; full replication repair postponed");
            return 0;
        }

        int totalRepaired = 0;
        for (StorageNodeEntity node : healthyNodes) {
            totalRepaired += repairNodeReplicas(node.getId());
        }
        return totalRepaired;
    }

    private boolean repairSingleFileReplica(FileEntity file, StorageNodeEntity targetNode, FileReplicaEntity existingReplica) {
        // Find a healthy surviving replica
        FileReplicaEntity survivingReplica = file.getReplicas().stream()
                .filter(r -> !r.getNode().getId().equals(targetNode.getId()))
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> "HEALTHY".equalsIgnoreCase(r.getNode().getStatus()) && !Boolean.TRUE.equals(r.getNode().getIsSimulatedDown()))
                .findFirst()
                .orElse(null);

        if (survivingReplica == null) {
            log.warn("Cannot repair file [{}]: No surviving healthy replica available", file.getId());
            return false;
        }

        StorageNodeEntity survivingNode = survivingReplica.getNode();
        StorageNodeClient.StorageRetrievalResult retrieval = storageClient.retrieveFileWithMetadata(
                survivingNode.getEndpoint(),
                survivingReplica.getStorageKey()
        );

        if (!retrieval.isSuccess() || retrieval.getContent() == null) {
            log.warn("Failed to retrieve file [{}] from surviving node [{}] during repair", file.getId(), survivingNode.getName());
            return false;
        }

        // Verify SHA-256 checksum on retrieved bytes before re-replicating
        byte[] bytes = retrieval.getContent();
        String recalculatedSha256 = FileService.calculateSha256(bytes);
        if (!recalculatedSha256.equalsIgnoreCase(file.getSha256Checksum())) {
            log.error("Integrity check failed during repair of file [{}]: expected {}, got {}",
                    file.getId(), file.getSha256Checksum(), recalculatedSha256);
            operationService.logOperation(
                    "INTEGRITY_MISMATCH",
                    file.getId(),
                    survivingNode.getId(),
                    "FAILED",
                    String.format("Repair aborted: Data corruption on surviving node %s (checksum mismatch).", survivingNode.getName())
            );
            return false;
        }

        // Replicate to target node
        String targetStorageKey = file.getId() + "_" + sanitizeStorageKey(file.getOriginalFilename());
        StorageResultDto writeResult = storageClient.storeFile(
                targetNode.getEndpoint(),
                targetStorageKey,
                bytes,
                file.getOriginalFilename(),
                file.getMimeType()
        );

        if (writeResult == null || !writeResult.isSuccess() || !recalculatedSha256.equalsIgnoreCase(writeResult.getSha256Checksum())) {
            log.error("Failed to write repaired replica for [{}] to target node [{}]", file.getId(), targetNode.getName());
            return false;
        }

        // Update or create replica record in PostgreSQL
        if (existingReplica != null) {
            existingReplica.setStatus("AVAILABLE");
            existingReplica.setStorageKey(targetStorageKey);
            existingReplica.setCreatedAt(OffsetDateTime.now());
            replicaRepository.save(existingReplica);
        } else {
            FileReplicaEntity newReplica = new FileReplicaEntity(
                    UUID.randomUUID().toString(),
                    file,
                    targetNode,
                    targetStorageKey,
                    "AVAILABLE",
                    OffsetDateTime.now()
            );
            replicaRepository.save(newReplica);
            file.getReplicas().add(newReplica);
        }

        // Check if all registered nodes now have AVAILABLE replicas
        List<StorageNodeEntity> allRegistered = nodeRepository.findAll();
        List<FileReplicaEntity> updatedReplicas = replicaRepository.findByFileId(file.getId());
        long availableCount = updatedReplicas.stream().filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus())).count();

        if (availableCount >= allRegistered.size() && allRegistered.size() >= 2) {
            file.setStatus("REPLICATED");
        } else if (availableCount > 0) {
            file.setStatus("DEGRADED");
        }
        fileRepository.save(file);

        // Audit log in operation_logs
        String opMsg = String.format("Repaired replica for file \"%s\" on %s from surviving copy on %s. SHA-256 verified (%s). Status: %s",
                file.getOriginalFilename(), targetNode.getName(), survivingNode.getName(), recalculatedSha256, file.getStatus());
        operationService.logOperation("REPLICA_REPAIR", file.getId(), targetNode.getId(), "SUCCESS", opMsg);
        log.info("{}", opMsg);

        return true;
    }

    private String sanitizeStorageKey(String filename) {
        if (filename == null || filename.isBlank()) return "unnamed.bin";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
