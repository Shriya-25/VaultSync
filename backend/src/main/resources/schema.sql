-- ==============================================================================
-- VaultSync — Minimum Distributed Backup Schema (PostgreSQL)
-- Phase 3 Specification
-- ==============================================================================

-- 1. Files metadata table
CREATE TABLE IF NOT EXISTS files (
    id VARCHAR(64) PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100),
    sha256_checksum VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Storage nodes registration table
CREATE TABLE IF NOT EXISTS storage_nodes (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    storage_directory VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    last_heartbeat TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. File replicas mapping table
CREATE TABLE IF NOT EXISTS file_replicas (
    id VARCHAR(64) PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL,
    node_id VARCHAR(50) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_file_replica_node UNIQUE (file_id, node_id),
    CONSTRAINT fk_replica_file FOREIGN KEY (file_id) REFERENCES files (id) ON DELETE CASCADE,
    CONSTRAINT fk_replica_node FOREIGN KEY (node_id) REFERENCES storage_nodes (id) ON DELETE CASCADE
);

-- 4. Operation logs table (audit trail for replication, failover, and verification)
CREATE TABLE IF NOT EXISTS operation_logs (
    id VARCHAR(64) PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL,
    file_id VARCHAR(64),
    node_id VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_files_status ON files (status);
CREATE INDEX IF NOT EXISTS idx_replicas_file_id ON file_replicas (file_id);
CREATE INDEX IF NOT EXISTS idx_replicas_node_id ON file_replicas (node_id);
CREATE INDEX IF NOT EXISTS idx_operation_logs_created_at ON operation_logs (created_at DESC);
