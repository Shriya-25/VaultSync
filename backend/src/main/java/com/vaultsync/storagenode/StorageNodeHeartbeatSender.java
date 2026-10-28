package com.vaultsync.storagenode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class StorageNodeHeartbeatSender {

    private static final Logger log = LoggerFactory.getLogger(StorageNodeHeartbeatSender.class);

    private final StorageNodeProperties properties;
    private final DiskStorageService storageService;
    private final RestTemplate restTemplate;

    @Value("${coordinator.url:http://localhost:8080}")
    private String coordinatorUrl;

    public StorageNodeHeartbeatSender(StorageNodeProperties properties,
                                      DiskStorageService storageService,
                                      RestTemplateBuilder builder) {
        this.properties = properties;
        this.storageService = storageService;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(1))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    /**
     * Send periodic heartbeat to coordinator
     */
    @Scheduled(fixedDelayString = "${node.heartbeat.interval-ms:5000}", initialDelay = 3000)
    public void sendHeartbeat() {
        if (properties.getId() == null || properties.getId().isBlank()) {
            return;
        }

        String url = coordinatorUrl + "/api/nodes/" + properties.getId() + "/heartbeat";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("nodeId", properties.getId());
            payload.put("nodeName", properties.getName());
            payload.put("status", "HEALTHY");
            payload.put("storageDirectory", storageService.getStorageStats().get("storageDirectory"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(url, request, Map.class);
            log.trace("Heartbeat sent from node [{}] to coordinator [{}]", properties.getId(), url);
        } catch (Exception e) {
            log.debug("Coordinator not reachable for heartbeat from node [{}]: {}", properties.getId(), e.getMessage());
        }
    }
}
