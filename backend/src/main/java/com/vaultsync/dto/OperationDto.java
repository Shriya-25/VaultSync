package com.vaultsync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class OperationDto {

    private String id;

    @JsonProperty("operation_type")
    private String operationType;

    @JsonProperty("file_id")
    private String fileId;

    @JsonProperty("node_id")
    private String nodeId;

    private String status;
    private String message;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public OperationDto() {
    }

    public OperationDto(String id, String operationType, String fileId, String nodeId,
                        String status, String message, OffsetDateTime createdAt) {
        this.id = id;
        this.operationType = operationType;
        this.fileId = fileId;
        this.nodeId = nodeId;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
