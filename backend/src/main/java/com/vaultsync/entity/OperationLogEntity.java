package com.vaultsync.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "operation_logs", indexes = {
    @Index(name = "idx_operation_logs_created_at", columnList = "created_at DESC")
})
public class OperationLogEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "operation_type", length = 50, nullable = false)
    private String operationType;

    @Column(name = "file_id", length = 64)
    private String fileId;

    @Column(name = "node_id", length = 50)
    private String nodeId;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public OperationLogEntity() {
    }

    public OperationLogEntity(String id, String operationType, String fileId, String nodeId,
                              String status, String message, OffsetDateTime createdAt) {
        this.id = id;
        this.operationType = operationType;
        this.fileId = fileId;
        this.nodeId = nodeId;
        this.status = status;
        this.message = message;
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
