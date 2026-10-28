package com.vaultsync.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "file_replicas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_file_replica_node", columnNames = {"file_id", "node_id"})
    },
    indexes = {
        @Index(name = "idx_replicas_file_id", columnList = "file_id"),
        @Index(name = "idx_replicas_node_id", columnList = "node_id")
    }
)
public class FileReplicaEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    private StorageNodeEntity node;

    @Column(name = "storage_key", length = 255, nullable = false)
    private String storageKey;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public FileReplicaEntity() {
    }

    public FileReplicaEntity(String id, FileEntity file, StorageNodeEntity node,
                             String storageKey, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.file = file;
        this.node = node;
        this.storageKey = storageKey;
        this.status = status;
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

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }

    public StorageNodeEntity getNode() {
        return node;
    }

    public void setNode(StorageNodeEntity node) {
        this.node = node;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
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
}
