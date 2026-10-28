package com.vaultsync.controller;

import com.vaultsync.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> getHealth() {
        String dbStatus = "DISCONNECTED";
        Map<String, Object> details = new HashMap<>();
        details.put("jvmVersion", System.getProperty("java.version"));
        details.put("os", System.getProperty("os.name"));

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                dbStatus = "CONNECTED";
                details.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                details.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
            }
        } catch (Exception e) {
            details.put("databaseError", e.getMessage() != null ? e.getMessage() : "Connection failed");
        }

        HealthResponse response = new HealthResponse(
                "UP",
                "VaultSync Coordinator",
                "1.0.0",
                "PHASE_2_3",
                dbStatus,
                details
        );

        return ResponseEntity.ok(response);
    }
}
