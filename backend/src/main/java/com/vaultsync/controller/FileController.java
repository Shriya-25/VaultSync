package com.vaultsync.controller;

import com.vaultsync.dto.FileDto;
import com.vaultsync.dto.RecoveryResultDto;
import com.vaultsync.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * GET /api/files
     * Returns the list of stored file metadata and their replica records.
     */
    @GetMapping
    public ResponseEntity<List<FileDto>> getAllFiles() {
        List<FileDto> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    /**
     * GET /api/files/{id}
     * Returns metadata and replica statuses for a single file.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFileById(@PathVariable String id) {
        FileDto file = fileService.getFileById(id);
        return ResponseEntity.ok(file);
    }

    /**
     * POST /api/files (and /api/files/upload)
     * Upload a file, calculate SHA-256 checksum, replicate across Node A and Node B,
     * persist metadata and replica statuses in PostgreSQL.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> uploadFile(@RequestParam("file") MultipartFile file) {
        FileDto result = fileService.uploadAndReplicateFile(file);
        HttpStatus status = "REPLICATED".equals(result.getStatus()) ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(result);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> uploadFileAlias(@RequestParam("file") MultipartFile file) {
        return uploadFile(file);
    }

    /**
     * POST /api/files/{id}/recover (and GET /api/files/{id}/recover)
     * Recovers file from a healthy replica, recalculates SHA-256,
     * verifies against original checksum, and returns the result.
     */
    @PostMapping("/{id}/recover")
    public ResponseEntity<RecoveryResultDto> recoverFile(@PathVariable String id) {
        RecoveryResultDto result = fileService.recoverAndVerifyFile(id);
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/{id}/recover")
    public ResponseEntity<RecoveryResultDto> recoverFileGet(@PathVariable String id) {
        return recoverFile(id);
    }

    /**
     * GET /api/files/{id}/download (and /api/files/{id}/content)
     * Streams file bytes through the coordinator, failing over to healthy replica
     * and verifying SHA-256 checksum integrity.
     */
    @GetMapping(value = {"/{id}/download", "/{id}/content"})
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        FileService.RetrievedFilePayload payload = fileService.downloadFile(id);
        ByteArrayResource resource = new ByteArrayResource(payload.content());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.filename() + "\"")
                .header("X-Original-Checksum", payload.originalSha256())
                .header("X-Recalculated-Checksum", payload.recalculatedSha256())
                .header("X-Retrieved-From-Node", payload.sourceNodeName())
                .contentType(MediaType.parseMediaType(payload.mimeType()))
                .contentLength(payload.content().length)
                .body(resource);
    }

    /**
     * DELETE /api/files/{id}
     * Deletes file metadata and removes physical replicas from storage nodes.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String id) {
        boolean deleted = fileService.deleteFile(id);
        return ResponseEntity.ok(Map.of(
                "success", deleted,
                "fileId", id,
                "message", "File and replicas successfully deleted"
        ));
    }
}
