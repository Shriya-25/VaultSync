package com.vaultsync.storagenode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class StorageNodeController {

    private static final Logger log = LoggerFactory.getLogger(StorageNodeController.class);

    private final DiskStorageService storageService;
    private final StorageNodeProperties properties;
    private final long startupTimeMillis;

    public StorageNodeController(DiskStorageService storageService, StorageNodeProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
        this.startupTimeMillis = System.currentTimeMillis();
    }

    /**
     * Node Health Check
     * GET /api/health or GET /health
     */
    @GetMapping({"/api/health", "/health"})
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("nodeId", properties.getId());
        response.put("nodeName", properties.getName());
        response.put("status", "HEALTHY");
        response.put("service", "VaultSync Storage Node Worker");
        response.put("uptimeSeconds", (System.currentTimeMillis() - startupTimeMillis) / 1000);
        response.put("timestamp", OffsetDateTime.now());

        Map<String, Object> stats = storageService.getStorageStats();
        response.putAll(stats);

        return ResponseEntity.ok(response);
    }

    /**
     * Store a file on this storage node
     * POST /api/storage/files
     */
    @PostMapping(value = "/api/storage/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StorageResultDto> storeFileMultipart(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storageKey", required = false) String storageKey
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        StorageResultDto.error(properties.getId(), "File payload cannot be empty")
                );
            }

            String key = (storageKey != null && !storageKey.isBlank())
                    ? storageKey
                    : properties.getId() + "_" + file.getOriginalFilename();

            StorageResultDto result = storageService.storeFile(
                    key,
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Failed to store file on node [{}]: {}", properties.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StorageResultDto.error(properties.getId(), "Write failed: " + e.getMessage()));
        }
    }

    /**
     * Store a file using raw byte body
     * POST /api/storage/files/raw
     */
    @PostMapping(value = "/api/storage/files/raw")
    public ResponseEntity<StorageResultDto> storeFileRaw(
            @RequestBody byte[] content,
            @RequestParam("storageKey") String storageKey,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "mimeType", required = false) String mimeType
    ) {
        try {
            if (content == null || content.length == 0) {
                return ResponseEntity.badRequest().body(
                        StorageResultDto.error(properties.getId(), "Byte content cannot be empty")
                );
            }

            StorageResultDto result = storageService.storeFile(
                    storageKey,
                    content,
                    filename != null ? filename : storageKey,
                    mimeType
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Failed to store raw bytes on node [{}]: {}", properties.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StorageResultDto.error(properties.getId(), "Write failed: " + e.getMessage()));
        }
    }

    /**
     * Retrieve a file by storageKey
     * GET /api/storage/files/{storageKey}
     */
    @GetMapping("/api/storage/files/{storageKey}")
    public ResponseEntity<Resource> retrieveFile(@PathVariable String storageKey) {
        try {
            Resource resource = storageService.getFileAsResource(storageKey);
            String sha256 = storageService.calculateFileSha256(storageKey);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storageKey + "\"")
                    .header("X-Storage-Node-Id", properties.getId())
                    .header("X-Storage-Key", storageKey)
                    .header("X-Checksum-SHA256", sha256)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (NoSuchFileException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Failed to retrieve file [{}] on node [{}]: {}", storageKey, properties.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a file by storageKey
     * DELETE /api/storage/files/{storageKey}
     */
    @DeleteMapping("/api/storage/files/{storageKey}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String storageKey) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = storageService.deleteFile(storageKey);
            response.put("success", deleted);
            response.put("nodeId", properties.getId());
            response.put("storageKey", storageKey);
            response.put("message", deleted ? "File deleted successfully" : "File not found");
            return deleted ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Failed to delete file [{}] on node [{}]: {}", storageKey, properties.getId(), e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify file integrity directly on this node
     * GET /api/storage/files/{storageKey}/verify
     */
    @GetMapping("/api/storage/files/{storageKey}/verify")
    public ResponseEntity<Map<String, Object>> verifyFile(@PathVariable String storageKey) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!storageService.fileExists(storageKey)) {
                response.put("exists", false);
                response.put("nodeId", properties.getId());
                response.put("storageKey", storageKey);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            long size = storageService.getFileSize(storageKey);
            String sha256 = storageService.calculateFileSha256(storageKey);

            response.put("exists", true);
            response.put("nodeId", properties.getId());
            response.put("storageKey", storageKey);
            response.put("sizeBytes", size);
            response.put("sha256Checksum", sha256);
            response.put("verifiedAt", OffsetDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exists", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * List all keys stored on this node
     * GET /api/storage/files
     */
    @GetMapping("/api/storage/files")
    public ResponseEntity<Map<String, Object>> listFiles() {
        try {
            List<String> keys = storageService.listStoredKeys();
            Map<String, Object> response = new HashMap<>();
            response.put("nodeId", properties.getId());
            response.put("nodeName", properties.getName());
            response.put("totalFiles", keys.size());
            response.put("files", keys);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

