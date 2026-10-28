package com.vaultsync.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public class HealthResponse {

    private String status;
    private String service;
    private String version;
    private String phase;
    private String database;
    private OffsetDateTime timestamp;
    private Map<String, Object> details;

    public HealthResponse() {
        this.timestamp = OffsetDateTime.now();
    }

    public HealthResponse(String status, String service, String version, String phase,
                          String database, Map<String, Object> details) {
        this.status = status;
        this.service = service;
        this.version = version;
        this.phase = phase;
        this.database = database;
        this.timestamp = OffsetDateTime.now();
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
