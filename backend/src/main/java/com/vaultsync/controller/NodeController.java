package com.vaultsync.controller;

import com.vaultsync.dto.NodeDto;
import com.vaultsync.service.NodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
