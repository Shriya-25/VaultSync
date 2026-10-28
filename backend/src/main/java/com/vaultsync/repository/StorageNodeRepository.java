package com.vaultsync.repository;

import com.vaultsync.entity.StorageNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageNodeRepository extends JpaRepository<StorageNodeEntity, String> {

    List<StorageNodeEntity> findAllByOrderByCreatedAtAsc();
}
