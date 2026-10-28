package com.vaultsync.controller;

import com.vaultsync.dto.NodeDto;
import com.vaultsync.service.NodeService;
import com.vaultsync.service.ReplicaRepairService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final NodeService nodeService;
    private final ReplicaRepairService repairService;

    public NodeController(NodeService nodeService, ReplicaRepairService repairService) {
        this.nodeService = nodeService;
        this.repairService = repairService;
    }

    /**
     * GET /api/nodes
     * Returns the registered storage-node records from PostgreSQL with live statuses.
     */
    @GetMapping
    public ResponseEntity<List<NodeDto>> getAllNodes() {
        List<NodeDto> nodes = nodeService.getAllNodes();
        return ResponseEntity.ok(nodes);
    }

    /**
     * GET /api/nodes/{id}
     * Returns details for a specific registered storage node.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NodeDto> getNodeById(@PathVariable String id) {
        NodeDto node = nodeService.getNodeById(id);
        return ResponseEntity.ok(node);
    }

    /**
     * POST /api/nodes/{id}/heartbeat
     * Receives heartbeat pulse sent from a storage node worker.
     */
    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Map<String, Object>> receiveHeartbeat(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        Map<String, Object> response = nodeService.processHeartbeat(id, payload);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/nodes/{id}/simulate-failure
     * Demo Control: Simulates storage node failure for classroom demonstration.
     */
    @PostMapping("/{id}/simulate-failure")
    public ResponseEntity<NodeDto> simulateFailure(@PathVariable String id) {
        NodeDto updated = nodeService.simulateNodeFailure(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/nodes/{id}/simulate-recovery
     * Demo Control: Restores a simulated storage node and triggers automated replica repair.
     */
    @PostMapping("/{id}/simulate-recovery")
    public ResponseEntity<NodeDto> simulateRecovery(@PathVariable String id) {
        NodeDto updated = nodeService.simulateNodeRecovery(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/nodes/check
     * Actively checks heartbeat timeouts and node reachability.
     */
    @PostMapping("/check")
    public ResponseEntity<List<NodeDto>> checkNodes() {
        List<NodeDto> updatedNodes = nodeService.refreshNodeStatuses();
        return ResponseEntity.ok(updatedNodes);
    }

    /**
     * POST /api/nodes/repair
     * Triggers an immediate replica repair sweep across all degraded files.
     */
    @PostMapping("/repair")
    public ResponseEntity<Map<String, Object>> triggerRepair() {
        int repairedCount = repairService.repairAllDegradedFiles();
        return ResponseEntity.ok(Map.of(
                "repairedFiles", repairedCount,
                "message", String.format("Repaired replicas for %d degraded file(s)", repairedCount)
        ));
    }

    /**
     * POST /api/nodes/{id}/test-storage
     * Performs an independent end-to-end write, read, and delete verification test on a storage node.
     */
    @PostMapping("/{id}/test-storage")
    public ResponseEntity<Map<String, Object>> testStorage(@PathVariable String id) {
        Map<String, Object> result = nodeService.testNodeStorage(id);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return success ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }
}
