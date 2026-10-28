package com.vaultsync.service;

import com.vaultsync.dto.FileDto;
import com.vaultsync.dto.RecoveryResultDto;
import com.vaultsync.dto.ReplicaDto;
import com.vaultsync.entity.FileEntity;
import com.vaultsync.entity.FileReplicaEntity;
import com.vaultsync.entity.StorageNodeEntity;
import com.vaultsync.exception.FileIntegrityException;
import com.vaultsync.exception.ResourceNotFoundException;
import com.vaultsync.repository.FileReplicaRepository;
import com.vaultsync.repository.FileRepository;
import com.vaultsync.repository.StorageNodeRepository;
import com.vaultsync.storagenode.StorageResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB

    private final FileRepository fileRepository;
    private final FileReplicaRepository fileReplicaRepository;
    private final StorageNodeRepository storageNodeRepository;
    private final StorageNodeClient storageNodeClient;
    private final OperationService operationService;

    public FileService(FileRepository fileRepository,
                       FileReplicaRepository fileReplicaRepository,
                       StorageNodeRepository storageNodeRepository,
                       StorageNodeClient storageNodeClient,
                       OperationService operationService) {
        this.fileRepository = fileRepository;
        this.fileReplicaRepository = fileReplicaRepository;
        this.storageNodeRepository = storageNodeRepository;
        this.storageNodeClient = storageNodeClient;
        this.operationService = operationService;
    }

    /**
     * Get all files and their replica records
     */
    @Transactional(readOnly = true)
    public List<FileDto> getAllFiles() {
        return fileRepository.findAllWithReplicas().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get single file metadata by id
     */
    @Transactional(readOnly = true)
    public FileDto getFileById(String id) {
        FileEntity entity = fileRepository.findByIdWithReplicas(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));
        return toDto(entity);
    }

    /**
     * Upload a file and replicate synchronously to all registered storage nodes
     * (Phase 5 Replication Engine)
     */
    @Transactional
    public FileDto uploadAndReplicateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file payload cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 25 MB limit (" + file.getSize() + " bytes)");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read upload file bytes: " + e.getMessage(), e);
        }

        String originalFilename = (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
                ? file.getOriginalFilename().trim()
                : "uploaded_file.bin";

        String mimeType = (file.getContentType() != null && !file.getContentType().isBlank())
                ? file.getContentType()
                : "application/octet-stream";

        String sha256Checksum = calculateSha256(content);
        String fileId = "f-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        FileEntity fileEntity = new FileEntity(
                fileId,
                originalFilename,
                (long) content.length,
                mimeType,
                sha256Checksum,
                "INITIALIZING",
                OffsetDateTime.now()
        );
        fileEntity.setLastRecoveryResult("UNTESTED");

        List<StorageNodeEntity> registeredNodes = storageNodeRepository.findAll();
        if (registeredNodes.isEmpty()) {
            fileEntity.setStatus("FAILED");
            fileRepository.save(fileEntity);
            operationService.logOperation("FILE_UPLOAD", fileId, null, "FAILED",
                    "No storage nodes registered in cluster. File upload aborted.");
            throw new IllegalStateException("No storage nodes registered in cluster");
        }

        List<FileReplicaEntity> replicas = new ArrayList<>();
        int successCount = 0;
        StringBuilder replicaSummary = new StringBuilder();

        for (StorageNodeEntity node : registeredNodes) {
            String storageKey = fileId + "_" + sanitizeStorageKey(originalFilename);
            StorageResultDto writeResult = storageNodeClient.storeFile(
                    node.getEndpoint(),
                    storageKey,
                    content,
                    originalFilename,
                    mimeType
            );

            boolean writeSuccess = writeResult != null
                    && writeResult.isSuccess()
                    && sha256Checksum.equalsIgnoreCase(writeResult.getSha256Checksum());

            String replicaStatus = writeSuccess ? "AVAILABLE" : "FAILED";
            if (writeSuccess) {
                successCount++;
            }

            FileReplicaEntity replica = new FileReplicaEntity(
                    UUID.randomUUID().toString(),
                    fileEntity,
                    node,
                    storageKey,
                    replicaStatus,
                    OffsetDateTime.now()
            );
            replicas.add(replica);

            if (replicaSummary.length() > 0) replicaSummary.append(", ");
            replicaSummary.append(node.getName()).append(": ").append(replicaStatus);
        }

        // Determine overall file backup status per PRD
        String overallStatus;
        if (successCount == registeredNodes.size() && registeredNodes.size() >= 2) {
            overallStatus = "REPLICATED";
        } else if (successCount > 0) {
            overallStatus = "DEGRADED";
        } else {
            overallStatus = "FAILED";
        }

        fileEntity.setStatus(overallStatus);
        fileEntity.setReplicas(replicas);

        FileEntity savedFile = fileRepository.save(fileEntity);

        String opStatus = "REPLICATED".equals(overallStatus) ? "SUCCESS"
                : ("DEGRADED".equals(overallStatus) ? "WARNING" : "FAILED");
        String opMessage = String.format("Uploaded \"%s\" (%d bytes, SHA-256: %s). Replicated to %d/%d nodes [%s]. Overall status: %s",
                originalFilename, content.length, sha256Checksum, successCount, registeredNodes.size(), replicaSummary, overallStatus);

        operationService.logOperation("FILE_UPLOAD", fileId, null, opStatus, opMessage);
        log.info("File upload & replication completed: {}", opMessage);

        return toDto(savedFile);
    }

    /**
     * Recover file from an available healthy replica and verify SHA-256 integrity
     * (Phase 5 Recovery & Integrity Engine)
     */
    @Transactional
    public RecoveryResultDto recoverAndVerifyFile(String fileId) {
        FileEntity file = fileRepository.findByIdWithReplicas(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        List<FileReplicaEntity> replicas = file.getReplicas();
        if (replicas == null || replicas.isEmpty()) {
            return RecoveryResultDto.failure(
                    fileId,
                    file.getOriginalFilename(),
                    file.getSha256Checksum(),
                    "No replica records found for file " + fileId
            );
        }

        // Prioritize replicas whose storage nodes are currently responsive
        List<FileReplicaEntity> candidates = new ArrayList<>(replicas);
        candidates.sort((r1, r2) -> {
            boolean h1 = storageNodeClient.checkNodeHealth(r1.getNode().getEndpoint()) != null;
            boolean h2 = storageNodeClient.checkNodeHealth(r2.getNode().getEndpoint()) != null;
            return Boolean.compare(h2, h1);
        });

        for (FileReplicaEntity replica : candidates) {
            StorageNodeEntity node = replica.getNode();
            StorageNodeClient.StorageRetrievalResult result = storageNodeClient.retrieveFileWithMetadata(
                    node.getEndpoint(),
                    replica.getStorageKey()
            );

            if (result.isSuccess() && result.getContent() != null) {
                byte[] bytes = result.getContent();
                String recalculatedChecksum = calculateSha256(bytes);
                boolean checksumVerified = recalculatedChecksum.equalsIgnoreCase(file.getSha256Checksum());

                if (checksumVerified) {
                    file.setLastRecoveryResult("VERIFIED (" + node.getName() + ")");
                    fileRepository.save(file);

                    String msg = String.format("Successfully recovered from healthy replica on %s. SHA-256 recalculated and verified identical to original upload.", node.getName());
                    operationService.logOperation("RECOVERY_VERIFY", fileId, node.getId(), "SUCCESS", msg);

                    return RecoveryResultDto.success(
                            fileId,
                            file.getOriginalFilename(),
                            node.getName(),
                            file.getSha256Checksum(),
                            recalculatedChecksum,
                            msg
                    );
                } else {
                    file.setLastRecoveryResult("FAILED (Checksum mismatch on " + node.getName() + ")");
                    fileRepository.save(file);

                    String msg = String.format("Integrity mismatch on node %s: expected %s, got %s",
                            node.getName(), file.getSha256Checksum(), recalculatedChecksum);
                    operationService.logOperation("INTEGRITY_MISMATCH", fileId, node.getId(), "FAILED", msg);
                    // Try next replica if available
                }
            }
        }

        // All replicas failed or unreachable
        file.setLastRecoveryResult("FAILED (No healthy replica)");
        fileRepository.save(file);

        String failureMsg = "Recovery failed: Both Storage Node A and Storage Node B are unavailable or unreadable. No healthy replica found.";
        operationService.logOperation("RECOVERY_VERIFY", fileId, null, "FAILED", failureMsg);

        return RecoveryResultDto.failure(
                fileId,
                file.getOriginalFilename(),
                file.getSha256Checksum(),
                failureMsg
        );
    }

    /**
     * Retrieve file content with failover and SHA-256 integrity verification
     */
    @Transactional(readOnly = true)
    public RetrievedFilePayload downloadFile(String fileId) {
        FileEntity file = fileRepository.findByIdWithReplicas(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        List<FileReplicaEntity> replicas = file.getReplicas();
        if (replicas == null || replicas.isEmpty()) {
            throw new ResourceNotFoundException("FileReplica", "fileId", fileId);
        }

        // Try healthy nodes first
        List<FileReplicaEntity> candidates = new ArrayList<>(replicas);
        candidates.sort((r1, r2) -> {
            boolean h1 = storageNodeClient.checkNodeHealth(r1.getNode().getEndpoint()) != null;
            boolean h2 = storageNodeClient.checkNodeHealth(r2.getNode().getEndpoint()) != null;
            return Boolean.compare(h2, h1);
        });

        for (FileReplicaEntity replica : candidates) {
            StorageNodeEntity node = replica.getNode();
            StorageNodeClient.StorageRetrievalResult result = storageNodeClient.retrieveFileWithMetadata(
                    node.getEndpoint(),
                    replica.getStorageKey()
            );

            if (result.isSuccess() && result.getContent() != null) {
                byte[] bytes = result.getContent();
                String recalculated = calculateSha256(bytes);
                if (!recalculated.equalsIgnoreCase(file.getSha256Checksum())) {
                    throw new FileIntegrityException(String.format(
                            "Data corruption detected: Recalculated SHA-256 [%s] does not match original [%s]",
                            recalculated, file.getSha256Checksum()
                    ));
                }

                operationService.logOperation("FILE_RETRIEVAL", fileId, node.getId(), "SUCCESS",
                        String.format("Retrieved file \"%s\" (%d bytes) from %s with verified SHA-256.",
                                file.getOriginalFilename(), bytes.length, node.getName()));

                return new RetrievedFilePayload(
                        bytes,
                        file.getOriginalFilename(),
                        file.getMimeType(),
                        file.getSha256Checksum(),
                        recalculated,
                        node.getName()
                );
            }
        }

        throw new IllegalStateException("All replicas for file [" + fileId + "] are unavailable or corrupted");
    }

    /**
     * Delete file and remove its replicas across all nodes
     */
    @Transactional
    public boolean deleteFile(String fileId) {
        FileEntity file = fileRepository.findByIdWithReplicas(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        for (FileReplicaEntity replica : file.getReplicas()) {
            storageNodeClient.deleteFile(replica.getNode().getEndpoint(), replica.getStorageKey());
        }

        fileRepository.delete(file);
        operationService.logOperation("FILE_DELETE", fileId, null, "SUCCESS",
                "Deleted file \"" + file.getOriginalFilename() + "\" and its replicas across cluster.");
        return true;
    }

    public FileDto toDto(FileEntity entity) {
        if (entity == null) return null;

        List<ReplicaDto> replicas = entity.getReplicas() != null
                ? entity.getReplicas().stream().map(this::toReplicaDto).collect(Collectors.toList())
                : List.of();

        return new FileDto(
                entity.getId(),
                entity.getOriginalFilename(),
                entity.getSizeBytes(),
                entity.getMimeType(),
                entity.getSha256Checksum(),
                entity.getStatus(),
                entity.getLastRecoveryResult(),
                entity.getCreatedAt(),
                replicas
        );
    }

    public ReplicaDto toReplicaDto(FileReplicaEntity replica) {
        if (replica == null) return null;
        String nodeName = replica.getNode() != null ? replica.getNode().getName() : null;
        return new ReplicaDto(
                replica.getId(),
                replica.getNode() != null ? replica.getNode().getId() : null,
                nodeName,
                replica.getStorageKey(),
                replica.getStatus(),
                replica.getCreatedAt()
        );
    }

    public static String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String sanitizeStorageKey(String filename) {
        if (filename == null || filename.isBlank()) {
            return "unnamed.bin";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Container for downloaded file bytes and verified metadata
     */
    public record RetrievedFilePayload(
            byte[] content,
            String filename,
            String mimeType,
            String originalSha256,
            String recalculatedSha256,
            String sourceNodeName
    ) {}
}
