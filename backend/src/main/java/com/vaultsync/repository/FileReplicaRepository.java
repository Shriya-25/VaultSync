package com.vaultsync.repository;

import com.vaultsync.entity.FileReplicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileReplicaRepository extends JpaRepository<FileReplicaEntity, String> {

    List<FileReplicaEntity> findByFileId(String fileId);

    Optional<FileReplicaEntity> findByFileIdAndNodeId(String fileId, String nodeId);

    List<FileReplicaEntity> findByNodeId(String nodeId);

    long countByNodeId(String nodeId);

    long countByNodeIdAndStatus(String nodeId, String status);
}
