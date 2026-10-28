package com.vaultsync.repository;

import com.vaultsync.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    @Query("SELECT DISTINCT f FROM FileEntity f LEFT JOIN FETCH f.replicas r LEFT JOIN FETCH r.node ORDER BY f.createdAt DESC")
    List<FileEntity> findAllWithReplicas();

    @Query("SELECT DISTINCT f FROM FileEntity f LEFT JOIN FETCH f.replicas r LEFT JOIN FETCH r.node WHERE f.id = :id")
    Optional<FileEntity> findByIdWithReplicas(@Param("id") String id);
}
