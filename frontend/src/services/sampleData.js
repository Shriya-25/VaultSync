/**
 * VaultSync — DCS Mini-Project
 * Phase 1 Temporary Sample Data (Aligned with reference design)
 */

export const INITIAL_STORAGE_NODES = [
  {
    id: 'node-1',
    node_name: 'Node A',
    identifier: 'node-a',
    base_url: 'http://localhost:8081',
    storage_path: '/storage/node-a',
    status: 'HEALTHY', // HEALTHY | UNAVAILABLE
    last_heartbeat_at: new Date(Date.now() - 3000).toISOString(),
    replica_count: 3,
    is_simulated_down: false,
  },
  {
    id: 'node-2',
    node_name: 'Node B',
    identifier: 'node-b',
    base_url: 'http://localhost:8082',
    storage_path: '/storage/node-b',
    status: 'HEALTHY',
    last_heartbeat_at: new Date(Date.now() - 2000).toISOString(),
    replica_count: 3,
    is_simulated_down: false,
  },
];

export const INITIAL_FILES = [
  {
    id: 'f-01',
    original_filename: 'project-report.pdf',
    size_bytes: 2516582, // 2.4 MB
    sha256_checksum: 'a3f5e2d1b6c42f98e7a9c1d5f3e6b8a77c4d2e1f089bb2198034e4a90f12bcde',
    backup_status: 'REPLICATED',
    created_at: new Date(Date.now() - 1000 * 60 * 35).toISOString(),
    last_recovery_result: 'VERIFIED',
    replicas: [
      {
        node_id: 'node-1',
        node_name: 'Node A',
        storage_key: 'node-a_project-report.bin',
        status: 'AVAILABLE',
      },
      {
        node_id: 'node-2',
        node_name: 'Node B',
        storage_key: 'node-b_project-report.bin',
        status: 'AVAILABLE',
      },
    ],
  },
  {
    id: 'f-02',
    original_filename: 'disaster-recovery-plan.docx',
    size_bytes: 3145728, // 3.0 MB
    sha256_checksum: 'd7c4e9a8f1b3d2c6e854911733cfb7b190d62c65bf0bcda32b57b277d9ad9f14',
    backup_status: 'REPLICATED',
    created_at: new Date(Date.now() - 1000 * 60 * 120).toISOString(),
    last_recovery_result: 'VERIFIED',
    replicas: [
      {
        node_id: 'node-1',
        node_name: 'Node A',
        storage_key: 'node-a_disaster-recovery-plan.bin',
        status: 'AVAILABLE',
      },
      {
        node_id: 'node-2',
        node_name: 'Node B',
        storage_key: 'node-b_disaster-recovery-plan.bin',
        status: 'AVAILABLE',
      },
    ],
  },
  {
    id: 'f-03',
    original_filename: 'cluster-diagram.png',
    size_bytes: 870400, // 850 KB
    sha256_checksum: 'f91e7c3a8d4b2f6e7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d67728',
    backup_status: 'REPLICATED',
    created_at: new Date(Date.now() - 1000 * 60 * 240).toISOString(),
    last_recovery_result: 'VERIFIED',
    replicas: [
      {
        node_id: 'node-1',
        node_name: 'Node A',
        storage_key: 'node-a_cluster-diagram.bin',
        status: 'AVAILABLE',
      },
      {
        node_id: 'node-2',
        node_name: 'Node B',
        storage_key: 'node-b_cluster-diagram.bin',
        status: 'AVAILABLE',
      },
    ],
  },
];
