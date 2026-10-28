package com.vaultsync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class ReplicaDto {

    private String id;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("node_name")
    private String nodeName;

    @JsonProperty("storage_key")
    private String storageKey;

    private String status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public ReplicaDto() {
    }

    public ReplicaDto(String id, String nodeId, String nodeName, String storageKey,
                      String status, OffsetDateTime createdAt) {
        this.id = id;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.storageKey = storageKey;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
