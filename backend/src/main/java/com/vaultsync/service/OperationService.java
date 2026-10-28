package com.vaultsync.service;

import com.vaultsync.dto.OperationDto;
import com.vaultsync.entity.OperationLogEntity;
import com.vaultsync.repository.OperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OperationService {

    private final OperationLogRepository operationRepository;

    public OperationService(OperationLogRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Transactional(readOnly = true)
    public List<OperationDto> getRecentOperations() {
        return operationRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OperationDto logOperation(String operationType, String fileId, String nodeId, String status, String message) {
        OperationLogEntity entity = new OperationLogEntity(
                UUID.randomUUID().toString(),
                operationType,
                fileId,
                nodeId,
                status,
                message,
                OffsetDateTime.now()
        );
        OperationLogEntity saved = operationRepository.save(entity);
        return toDto(saved);
    }

    public OperationDto toDto(OperationLogEntity entity) {
        if (entity == null) return null;
        return new OperationDto(
                entity.getId(),
                entity.getOperationType(),
                entity.getFileId(),
                entity.getNodeId(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }
}
