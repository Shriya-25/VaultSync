package com.vaultsync.repository;

import com.vaultsync.entity.OperationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLogEntity, String> {

    List<OperationLogEntity> findTop20ByOrderByCreatedAtDesc();
}
