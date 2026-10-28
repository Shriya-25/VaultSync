package com.vaultsync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HeartbeatMonitorService {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatMonitorService.class);

    private final NodeService nodeService;

    @Value("${vaultsync.heartbeat.timeout-seconds:10}")
    private long timeoutSeconds;

    public HeartbeatMonitorService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Periodic background task to detect node heartbeat timeouts and failure states
     */
    @Scheduled(fixedDelayString = "${vaultsync.heartbeat.monitor-interval-ms:5000}", initialDelay = 5000)
    public void monitorNodeHeartbeats() {
        try {
            nodeService.checkHeartbeatsAndFailures(timeoutSeconds);
        } catch (Exception e) {
            log.error("Error running heartbeat monitor: {}", e.getMessage(), e);
        }
    }
}
