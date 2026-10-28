package com.vaultsync.storagenode;

import java.time.OffsetDateTime;

public class StorageResultDto {

    private boolean success;
    private String nodeId;
    private String nodeName;
    private String storageKey;
    private String originalFilename;
    private long sizeBytes;
    private String sha256Checksum;
    private String mimeType;
    private String storagePath;
    private OffsetDateTime timestamp;
    private String message;

    public StorageResultDto() {
        this.timestamp = OffsetDateTime.now();
    }

    public StorageResultDto(boolean success, String nodeId, String nodeName, String storageKey,
                            String originalFilename, long sizeBytes, String sha256Checksum,
                            String mimeType, String storagePath, String message) {
        this.success = success;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.sizeBytes = sizeBytes;
        this.sha256Checksum = sha256Checksum;
        this.mimeType = mimeType;
        this.storagePath = storagePath;
        this.timestamp = OffsetDateTime.now();
        this.message = message;
    }

    public static StorageResultDto success(String nodeId, String nodeName, String storageKey,
                                          String originalFilename, long sizeBytes,
                                          String sha256Checksum, String mimeType, String storagePath) {
        return new StorageResultDto(true, nodeId, nodeName, storageKey, originalFilename,
                sizeBytes, sha256Checksum, mimeType, storagePath, "File stored successfully");
    }

    public static StorageResultDto error(String nodeId, String message) {
        StorageResultDto dto = new StorageResultDto();
        dto.setSuccess(false);
        dto.setNodeId(nodeId);
        dto.setMessage(message);
        return dto;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getSha256Checksum() {
        return sha256Checksum;
    }

    public void setSha256Checksum(String sha256Checksum) {
        this.sha256Checksum = sha256Checksum;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

