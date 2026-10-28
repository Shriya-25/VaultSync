package com.vaultsync.service;

import com.vaultsync.storagenode.StorageResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Map;

@Component
public class StorageNodeClient {

    private static final Logger log = LoggerFactory.getLogger(StorageNodeClient.class);

    private final RestTemplate restTemplate;

    public StorageNodeClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Check if a remote storage node is healthy and reachable
     */
    public Map<String, Object> checkNodeHealth(String endpoint) {
        try {
            String url = normalizeEndpoint(endpoint) + "/api/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (ResourceAccessException e) {
            log.debug("Storage node at [{}] is unreachable: {}", endpoint, e.getMessage());
        } catch (Exception e) {
            log.warn("Error probing storage node at [{}]: {}", endpoint, e.getMessage());
        }
        return null;
    }

    /**
     * Store file bytes on remote storage node
     */
    public StorageResultDto storeFile(String endpoint, String storageKey, byte[] content,
                                      String originalFilename, String mimeType) {
        String url = UriComponentsBuilder.fromHttpUrl(normalizeEndpoint(endpoint) + "/api/storage/files/raw")
                .queryParam("storageKey", storageKey)
                .queryParam("filename", originalFilename != null ? originalFilename : storageKey)
                .queryParam("mimeType", mimeType != null ? mimeType : "application/octet-stream")
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<byte[]> entity = new HttpEntity<>(content, headers);

        try {
            ResponseEntity<StorageResultDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    StorageResultDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to store file [{}] on node [{}]: {}", storageKey, endpoint, e.getMessage());
            return StorageResultDto.error(endpoint, "Storage failed: " + e.getMessage());
        }
    }

    /**
     * Retrieve file bytes from remote storage node
     */
    public byte[] retrieveFile(String endpoint, String storageKey) {
        StorageRetrievalResult result = retrieveFileWithMetadata(endpoint, storageKey);
        return result.isSuccess() ? result.getContent() : null;
    }

    /**
     * Retrieve file bytes and response metadata from remote storage node
     */
    public StorageRetrievalResult retrieveFileWithMetadata(String endpoint, String storageKey) {
        String url = normalizeEndpoint(endpoint) + "/api/storage/files/" + storageKey;
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String checksum = response.getHeaders().getFirst("X-Checksum-SHA256");
                String nodeId = response.getHeaders().getFirst("X-Storage-Node-Id");
                return new StorageRetrievalResult(true, response.getBody(), checksum, nodeId, null);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve file [{}] from node [{}]: {}", storageKey, endpoint, e.getMessage());
            return new StorageRetrievalResult(false, null, null, null, e.getMessage());
        }
        return new StorageRetrievalResult(false, null, null, null, "Empty response from storage node");
    }

    /**
     * Delete file from remote storage node
     */
    public boolean deleteFile(String endpoint, String storageKey) {
        String url = normalizeEndpoint(endpoint) + "/api/storage/files/" + storageKey;
        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            log.warn("Failed to delete file [{}] from node [{}]: {}", storageKey, endpoint, e.getMessage());
            return false;
        }
    }

    /**
     * Verify file checksum directly on remote storage node
     */
    public Map<String, Object> verifyFile(String endpoint, String storageKey) {
        String url = normalizeEndpoint(endpoint) + "/api/storage/files/" + storageKey + "/verify";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.warn("Failed to verify file [{}] on node [{}]: {}", storageKey, endpoint, e.getMessage());
            return null;
        }
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null) return "http://localhost:8081";
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }

    /**
     * Structured result for remote storage retrieval
     */
    public static class StorageRetrievalResult {
        private final boolean success;
        private final byte[] content;
        private final String checksum;
        private final String nodeId;
        private final String errorMessage;

        public StorageRetrievalResult(boolean success, byte[] content, String checksum,
                                      String nodeId, String errorMessage) {
            this.success = success;
            this.content = content;
            this.checksum = checksum;
            this.nodeId = nodeId;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public byte[] getContent() {
            return content;
        }

        public String getChecksum() {
            return checksum;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
