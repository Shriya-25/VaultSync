package com.vaultsync.service;

import com.vaultsync.dto.NodeDto;
import com.vaultsync.entity.StorageNodeEntity;
import com.vaultsync.exception.ResourceNotFoundException;
import com.vaultsync.repository.StorageNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NodeService {

    private final StorageNodeRepository nodeRepository;

    public NodeService(StorageNodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Transactional(readOnly = true)
    public List<NodeDto> getAllNodes() {
        return nodeRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NodeDto getNodeById(String id) {
        StorageNodeEntity entity = nodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StorageNode", "id", id));
        return toDto(entity);
    }

    public NodeDto toDto(StorageNodeEntity entity) {
        if (entity == null) return null;
        return new NodeDto(
                entity.getId(),
                entity.getName(),
                entity.getEndpoint(),
                entity.getStorageDirectory(),
                entity.getStatus(),
                entity.getLastHeartbeat(),
                entity.getCreatedAt()
        );
    }
}
