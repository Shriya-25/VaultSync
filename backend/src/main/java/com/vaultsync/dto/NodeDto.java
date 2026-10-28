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

    @JsonProperty("is_simulated_down")
    private Boolean isSimulatedDown = false;

    @JsonProperty("replica_count")
    private Long replicaCount = 0L;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public NodeDto() {
    }

    public NodeDto(String id, String name, String endpoint, String storageDirectory,
                   String status, OffsetDateTime lastHeartbeat, OffsetDateTime createdAt) {
        this(id, name, endpoint, storageDirectory, status, lastHeartbeat, false, 0L, createdAt);
    }

    public NodeDto(String id, String name, String endpoint, String storageDirectory,
                   String status, OffsetDateTime lastHeartbeat, Boolean isSimulatedDown,
                   Long replicaCount, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.endpoint = endpoint;
        this.storageDirectory = storageDirectory;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
        this.isSimulatedDown = isSimulatedDown != null ? isSimulatedDown : false;
        this.replicaCount = replicaCount != null ? replicaCount : 0L;
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

    @JsonProperty("node_name")
    public String getNodeName() {
        return name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty("base_url")
    public String getBaseUrl() {
        return endpoint;
    }

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    @JsonProperty("storage_path")
    public String getStoragePath() {
        return storageDirectory;
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

    @JsonProperty("last_heartbeat_at")
    public OffsetDateTime getLastHeartbeatAt() {
        return lastHeartbeat;
    }

    public Boolean getIsSimulatedDown() {
        return isSimulatedDown;
    }

    public void setIsSimulatedDown(Boolean isSimulatedDown) {
        this.isSimulatedDown = isSimulatedDown;
    }

    public Long getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(Long replicaCount) {
        this.replicaCount = replicaCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
