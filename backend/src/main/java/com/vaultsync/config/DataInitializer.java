package com.vaultsync.config;

import com.vaultsync.entity.OperationLogEntity;
import com.vaultsync.entity.StorageNodeEntity;
import com.vaultsync.repository.OperationLogRepository;
import com.vaultsync.repository.StorageNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StorageNodeRepository nodeRepository;
    private final OperationLogRepository operationRepository;

    public DataInitializer(StorageNodeRepository nodeRepository, OperationLogRepository operationRepository) {
        this.nodeRepository = nodeRepository;
        this.operationRepository = operationRepository;
    }

    @Override
    public void run(String... args) {
        try {
            initializeStorageNodes();
        } catch (Exception e) {
            log.warn("Database initialization deferred: {}", e.getMessage());
        }
    }

    private void initializeStorageNodes() {
        if (!nodeRepository.existsById("node-a")) {
            StorageNodeEntity nodeA = new StorageNodeEntity(
                    "node-a",
                    "Node A",
                    "http://localhost:8081",
                    "/storage/node-a",
                    "INITIALIZING",
                    null,
                    OffsetDateTime.now()
            );
            nodeRepository.save(nodeA);
            log.info("Registered initial storage node: Node A (node-a)");
        }

        if (!nodeRepository.existsById("node-b")) {
            StorageNodeEntity nodeB = new StorageNodeEntity(
                    "node-b",
                    "Node B",
                    "http://localhost:8082",
                    "/storage/node-b",
                    "INITIALIZING",
                    null,
                    OffsetDateTime.now()
            );
            nodeRepository.save(nodeB);
            log.info("Registered initial storage node: Node B (node-b)");
        }

        if (operationRepository.count() == 0) {
            OperationLogEntity initLog = new OperationLogEntity(
                    UUID.randomUUID().toString(),
                    "SYSTEM_INIT",
                    null,
                    null,
                    "SUCCESS",
                    "VaultSync coordinator initialized with registered storage nodes Node A and Node B.",
                    OffsetDateTime.now()
            );
            operationRepository.save(initLog);
            log.info("Recorded initial system audit log");
        }
    }
}
