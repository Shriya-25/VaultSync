package com.vaultsync.service;

import com.vaultsync.dto.FileDto;
import com.vaultsync.dto.ReplicaDto;
import com.vaultsync.entity.FileEntity;
import com.vaultsync.entity.FileReplicaEntity;
import com.vaultsync.exception.ResourceNotFoundException;
import com.vaultsync.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional(readOnly = true)
    public List<FileDto> getAllFiles() {
        return fileRepository.findAllWithReplicas().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileDto getFileById(String id) {
        FileEntity entity = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));
        return toDto(entity);
    }

    public FileDto toDto(FileEntity entity) {
        if (entity == null) return null;

        List<ReplicaDto> replicas = entity.getReplicas() != null
                ? entity.getReplicas().stream().map(this::toReplicaDto).collect(Collectors.toList())
                : List.of();

        return new FileDto(
                entity.getId(),
                entity.getOriginalFilename(),
                entity.getSizeBytes(),
                entity.getMimeType(),
                entity.getSha256Checksum(),
                entity.getStatus(),
                entity.getCreatedAt(),
                replicas
        );
    }

    public ReplicaDto toReplicaDto(FileReplicaEntity replica) {
        if (replica == null) return null;
        String nodeName = replica.getNode() != null ? replica.getNode().getName() : null;
        return new ReplicaDto(
                replica.getId(),
                replica.getNode() != null ? replica.getNode().getId() : null,
                nodeName,
                replica.getStorageKey(),
                replica.getStatus(),
                replica.getCreatedAt()
        );
    }
}
