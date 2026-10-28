package com.vaultsync;

import com.vaultsync.storagenode.StorageNodeApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@EnableScheduling
@SpringBootApplication(
        scanBasePackages = {
                "com.vaultsync.config",
                "com.vaultsync.controller",
                "com.vaultsync.service",
                "com.vaultsync.repository",
                "com.vaultsync.entity",
                "com.vaultsync.exception",
                "com.vaultsync.dto"
        }
)
public class VaultSyncApplication {

    public static void main(String[] args) {
        boolean isStorageNode = Arrays.stream(args).anyMatch(arg ->
                arg.contains("storage-node") ||
                arg.contains("node.id") ||
                arg.contains("node.dir") ||
                arg.contains("app.mode=storage-node")
        ) || Boolean.getBoolean("storage-node") || "true".equalsIgnoreCase(System.getenv("STORAGE_NODE_MODE"));

        if (isStorageNode) {
            StorageNodeApplication.main(args);
        } else {
            SpringApplication.run(VaultSyncApplication.class, args);
        }
    }
}
