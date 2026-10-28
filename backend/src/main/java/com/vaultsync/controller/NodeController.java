package com.vaultsync.controller;

import com.vaultsync.dto.NodeDto;
import com.vaultsync.service.NodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final NodeService nodeService;

    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * GET /api/nodes
     * Returns the registered storage-node records from the database.
     * Probes live health to keep PostgreSQL records updated.
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
     * POST /api/nodes/check
     * Actively probes both Node A and Node B and updates PostgreSQL status and heartbeat.
     */
    @PostMapping("/check")
    public ResponseEntity<List<NodeDto>> checkNodes() {
        List<NodeDto> updatedNodes = nodeService.refreshNodeStatuses();
        return ResponseEntity.ok(updatedNodes);
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
