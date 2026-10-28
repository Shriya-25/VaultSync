package com.vaultsync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class NodeDto {

    private String id;
    private String name;
    private String endpoint;

    @JsonProperty("storage_directory")
    private String storageDirectory;

    private String status;

    @JsonProperty("last_heartbeat")
    private OffsetDateTime lastHeartbeat;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public NodeDto() {
    }

    public NodeDto(String id, String name, String endpoint, String storageDirectory,
                   String status, OffsetDateTime lastHeartbeat, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.endpoint = endpoint;
        this.storageDirectory = storageDirectory;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
        this.createdAt = createdAt;
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
