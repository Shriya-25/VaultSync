package com.vaultsync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileDto {

    private String id;

    @JsonProperty("original_filename")
    private String originalFilename;

    @JsonProperty("size_bytes")
    private Long sizeBytes;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("sha256_checksum")
    private String sha256Checksum;

    private String status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private List<ReplicaDto> replicas = new ArrayList<>();

    public FileDto() {
    }

    public FileDto(String id, String originalFilename, Long sizeBytes, String mimeType,
                   String sha256Checksum, String status, OffsetDateTime createdAt, List<ReplicaDto> replicas) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.sizeBytes = sizeBytes;
        this.mimeType = mimeType;
        this.sha256Checksum = sha256Checksum;
        this.status = status;
        this.createdAt = createdAt;
        this.replicas = replicas != null ? replicas : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSha256Checksum() {
        return sha256Checksum;
    }

    public void setSha256Checksum(String sha256Checksum) {
        this.sha256Checksum = sha256Checksum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ReplicaDto> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<ReplicaDto> replicas) {
        this.replicas = replicas;
    }
}
