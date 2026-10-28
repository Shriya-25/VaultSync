package com.vaultsync.storagenode;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DiskStorageService {

    private static final Logger log = LoggerFactory.getLogger(DiskStorageService.class);

    private final StorageNodeProperties properties;
    private Path storageBasePath;

    public DiskStorageService(StorageNodeProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        this.storageBasePath = Paths.get(properties.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageBasePath);
            log.info("Initialized storage node [{}] at path: {}", properties.getId(), this.storageBasePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + this.storageBasePath, e);
        }
    }

    public StorageResultDto storeFile(String storageKey, byte[] content, String originalFilename, String mimeType) throws IOException {
        String safeKey = sanitizeKey(storageKey);
        Path targetPath = resolvePath(safeKey);

        Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        String sha256 = calculateSha256(content);

        log.info("Node [{}] stored file [{}] ({} bytes, sha256: {})",
                properties.getId(), safeKey, content.length, sha256);

        return StorageResultDto.success(
                properties.getId(),
                properties.getName(),
                safeKey,
                originalFilename != null ? originalFilename : safeKey,
                content.length,
                sha256,
                mimeType != null ? mimeType : "application/octet-stream",
                targetPath.toString()
        );
    }

    public Resource getFileAsResource(String storageKey) throws IOException {
        String safeKey = sanitizeKey(storageKey);
        Path targetPath = resolvePath(safeKey);

        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new NoSuchFileException("File not found on node [" + properties.getId() + "]: " + safeKey);
        }

        return new FileSystemResource(targetPath);
    }

    public byte[] getFileBytes(String storageKey) throws IOException {
        String safeKey = sanitizeKey(storageKey);
        Path targetPath = resolvePath(safeKey);

        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new NoSuchFileException("File not found on node [" + properties.getId() + "]: " + safeKey);
        }

        return Files.readAllBytes(targetPath);
    }

    public boolean deleteFile(String storageKey) throws IOException {
        String safeKey = sanitizeKey(storageKey);
        Path targetPath = resolvePath(safeKey);
        boolean deleted = Files.deleteIfExists(targetPath);
        if (deleted) {
            log.info("Node [{}] deleted file [{}]", properties.getId(), safeKey);
        }
        return deleted;
    }

    public String calculateFileSha256(String storageKey) throws IOException {
        byte[] bytes = getFileBytes(storageKey);
        return calculateSha256(bytes);
    }

    public boolean fileExists(String storageKey) {
        try {
            Path targetPath = resolvePath(sanitizeKey(storageKey));
            return Files.exists(targetPath) && Files.isRegularFile(targetPath);
        } catch (Exception e) {
            return false;
        }
    }

    public long getFileSize(String storageKey) throws IOException {
        Path targetPath = resolvePath(sanitizeKey(storageKey));
        return Files.size(targetPath);
    }

    public List<String> listStoredKeys() throws IOException {
        try (Stream<Path> stream = Files.list(this.storageBasePath)) {
            return stream.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        long count = 0;
        long totalBytes = 0;

        try (Stream<Path> stream = Files.list(this.storageBasePath)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            count = files.size();
            for (Path f : files) {
                totalBytes += Files.size(f);
            }
        } catch (Exception e) {
            log.warn("Error calculating storage stats for node [{}]: {}", properties.getId(), e.getMessage());
        }

        stats.put("totalFiles", count);
        stats.put("totalBytes", totalBytes);
        stats.put("storageDirectory", this.storageBasePath.toString());
        return stats;
    }

    public Path getStorageBasePath() {
        return storageBasePath;
    }

    private Path resolvePath(String safeKey) {
        Path resolved = storageBasePath.resolve(safeKey).normalize();
        if (!resolved.startsWith(storageBasePath)) {
            throw new SecurityException("Directory traversal attack detected: " + safeKey);
        }
        return resolved;
    }

    private String sanitizeKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }
        return key.replaceAll("[/\\\\:]", "_");
    }

    public static String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

