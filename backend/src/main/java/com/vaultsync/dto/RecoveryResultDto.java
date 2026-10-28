package com.vaultsync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecoveryResultDto {

    private boolean success;

    @JsonProperty("fileId")
    private String fileId;

    private String filename;

    @JsonProperty("recoveredFrom")
    private String recoveredFrom;

    @JsonProperty("checksumVerified")
    private boolean checksumVerified;

    @JsonProperty("originalChecksum")
    private String originalChecksum;

    @JsonProperty("recalculatedChecksum")
    private String recalculatedChecksum;

    private String message;

    public RecoveryResultDto() {
    }

    public RecoveryResultDto(boolean success, String fileId, String filename, String recoveredFrom,
                             boolean checksumVerified, String originalChecksum,
                             String recalculatedChecksum, String message) {
        this.success = success;
        this.fileId = fileId;
        this.filename = filename;
        this.recoveredFrom = recoveredFrom;
        this.checksumVerified = checksumVerified;
        this.originalChecksum = originalChecksum;
        this.recalculatedChecksum = recalculatedChecksum;
        this.message = message;
    }

    public static RecoveryResultDto success(String fileId, String filename, String recoveredFrom,
                                           String originalChecksum, String recalculatedChecksum,
                                           String message) {
        return new RecoveryResultDto(true, fileId, filename, recoveredFrom, true,
                originalChecksum, recalculatedChecksum, message);
    }

    public static RecoveryResultDto failure(String fileId, String filename, String originalChecksum,
                                           String message) {
        return new RecoveryResultDto(false, fileId, filename, null, false,
                originalChecksum, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getRecoveredFrom() {
        return recoveredFrom;
    }

    public void setRecoveredFrom(String recoveredFrom) {
        this.recoveredFrom = recoveredFrom;
    }

    public boolean isChecksumVerified() {
        return checksumVerified;
    }

    public void setChecksumVerified(boolean checksumVerified) {
        this.checksumVerified = checksumVerified;
    }

    public String getOriginalChecksum() {
        return originalChecksum;
    }

    public void setOriginalChecksum(String originalChecksum) {
        this.originalChecksum = originalChecksum;
    }

    public String getRecalculatedChecksum() {
        return recalculatedChecksum;
    }

    public void setRecalculatedChecksum(String recalculatedChecksum) {
        this.recalculatedChecksum = recalculatedChecksum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

