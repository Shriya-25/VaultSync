package com.vaultsync.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "storage_nodes")
public class StorageNodeEntity {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "endpoint", length = 255, nullable = false)
    private String endpoint;

    @Column(name = "storage_directory", length = 255, nullable = false)
    private String storageDirectory;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "last_heartbeat")
    private OffsetDateTime lastHeartbeat;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public StorageNodeEntity() {
    }

    public StorageNodeEntity(String id, String name, String endpoint, String storageDirectory,
                             String status, OffsetDateTime lastHeartbeat, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.endpoint = endpoint;
        this.storageDirectory = storageDirectory;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(OffsetDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
