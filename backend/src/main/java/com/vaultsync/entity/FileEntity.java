package com.vaultsync.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "files", indexes = {
    @Index(name = "idx_files_status", columnList = "status")
})
public class FileEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "original_filename", length = 255, nullable = false)
    private String originalFilename;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "sha256_checksum", length = 64, nullable = false)
    private String sha256Checksum;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "last_recovery_result", length = 100)
    private String lastRecoveryResult = "UNTESTED";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FileReplicaEntity> replicas = new ArrayList<>();

    public FileEntity() {
    }

    public FileEntity(String id, String originalFilename, Long sizeBytes, String mimeType,
                      String sha256Checksum, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.sizeBytes = sizeBytes;
        this.mimeType = mimeType;
        this.sha256Checksum = sha256Checksum;
        this.status = status;
        this.lastRecoveryResult = "UNTESTED";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
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

    public String getLastRecoveryResult() {
        return lastRecoveryResult;
    }

    public void setLastRecoveryResult(String lastRecoveryResult) {
        this.lastRecoveryResult = lastRecoveryResult;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<FileReplicaEntity> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<FileReplicaEntity> replicas) {
        this.replicas = replicas;
    }
}
