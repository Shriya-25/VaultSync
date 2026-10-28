package com.vaultsync.controller;

import com.vaultsync.dto.OperationDto;
import com.vaultsync.service.OperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    /**
     * GET /api/operations
     * Returns recent operation records (audit trail).
     */
    @GetMapping
    public ResponseEntity<List<OperationDto>> getRecentOperations() {
        List<OperationDto> operations = operationService.getRecentOperations();
        return ResponseEntity.ok(operations);
    }
}
