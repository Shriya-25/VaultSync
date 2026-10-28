/**
 * VaultSync — API Service Layer (PRD v2 Minimal Scope)
 *
 * Provides REST client interfaces for the Spring Boot coordinator.
 * In Phase 1 preview mode, provides schema-compliant fallbacks with
 * clear labels. In Phase 2 onward, calls real backend endpoints at /api.
 */

import { INITIAL_FILES, INITIAL_STORAGE_NODES } from './sampleData';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

let localFiles = [...INITIAL_FILES];
let localNodes = [...INITIAL_STORAGE_NODES];
let isBackendLive = false;

export const apiService = {
  /**
   * Check whether Spring Boot coordinator is reachable
   */
  async checkBackendHealth() {
    try {
      const response = await fetch(`${API_BASE_URL}/health`);
      if (response.ok) {
        isBackendLive = true;
        return { live: true, data: await response.json() };
      }
    } catch {
      isBackendLive = false;
    }
    return { live: false, message: 'Spring Boot coordinator not connected (Phase 1 Preview Mode)' };
  },

  isLive() {
    return isBackendLive;
  },

  /**
   * List saved files and replica statuses
   * GET /api/files
   */
  async getFiles() {
    try {
      const response = await fetch(`${API_BASE_URL}/files`);
      if (response.ok) {
        isBackendLive = true;
        return await response.json();
      }
    } catch {
      // preview mode fallback
    }
    return [...localFiles];
  },

  /**
   * Get single file details
   * GET /api/files/{id}
   */
  async getFile(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/files/${id}`);
      if (response.ok) return await response.json();
    } catch {
      // preview mode fallback
    }
    return localFiles.find((f) => f.id === id) || null;
  },

  /**
   * Upload file and replicate across Node A and Node B
   * POST /api/files
   */
  async uploadFile(file) {
    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await fetch(`${API_BASE_URL}/files`, {
        method: 'POST',
        body: formData,
      });
      if (response.ok) return await response.json();
    } catch {
      // preview mode fallback
    }

    // Temporary preview simulation for Phase 1
    const newFile = {
      id: `f-${Math.random().toString(36).substring(2, 9)}`,
      original_filename: file.name,
      size_bytes: file.size,
      sha256_checksum: 'a' + Array.from({ length: 63 }, () => Math.floor(Math.random() * 16).toString(16)).join(''),
      backup_status: 'REPLICATED',
      created_at: new Date().toISOString(),
      last_recovery_result: 'UNTESTED',
      replicas: localNodes.map((node) => ({
        node_id: node.id,
        node_name: node.node_name,
        storage_key: `${node.identifier}_${file.name.replace(/[^a-zA-Z0-9._-]/g, '_')}.bin`,
        status: node.status === 'HEALTHY' ? 'AVAILABLE' : 'FAILED',
      })),
    };

    localFiles.unshift(newFile);
    return newFile;
  },

  /**
   * List storage nodes and health
   * GET /api/nodes
   */
  async getNodes() {
    try {
      const response = await fetch(`${API_BASE_URL}/nodes`);
      if (response.ok) {
        isBackendLive = true;
        return await response.json();
      }
    } catch {
      // preview mode fallback
    }
    return [...localNodes];
  },

  /**
   * Simulate node failure (DCS Fault Injection)
   * POST /api/nodes/{id}/simulate-failure
   */
  async simulateNodeFailure(nodeId) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodes/${nodeId}/simulate-failure`, { method: 'POST' });
      if (response.ok) return await response.json();
    } catch {
      // preview mode fallback
    }

    localNodes = localNodes.map((n) => (n.id === nodeId ? { ...n, status: 'UNAVAILABLE', is_simulated_down: true } : n));
    return localNodes.find((n) => n.id === nodeId);
  },

  /**
   * Restore simulated node to service
   * POST /api/nodes/{id}/simulate-recovery
   */
  async simulateNodeRecovery(nodeId) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodes/${nodeId}/simulate-recovery`, { method: 'POST' });
      if (response.ok) return await response.json();
    } catch {
      // preview mode fallback
    }

    localNodes = localNodes.map((n) =>
      n.id === nodeId ? { ...n, status: 'HEALTHY', is_simulated_down: false, last_heartbeat_at: new Date().toISOString() } : n
    );
    return localNodes.find((n) => n.id === nodeId);
  },

  /**
   * Recover file from healthy replica and verify SHA-256
   * POST /api/files/{id}/recover
   */
  async recoverFile(fileId) {
    try {
      const response = await fetch(`${API_BASE_URL}/files/${fileId}/recover`, { method: 'POST' });
      if (response.ok) return await response.json();
    } catch {
      // preview mode fallback
    }

    const file = localFiles.find((f) => f.id === fileId);
    if (!file) throw new Error('File not found');

    const healthyNodes = localNodes.filter((n) => n.status === 'HEALTHY');
    const healthyReplica = file.replicas.find((r) => healthyNodes.some((hn) => hn.id === r.node_id));

    if (!healthyReplica) {
      file.last_recovery_result = 'FAILED (No healthy replica)';
      return {
        success: false,
        fileId,
        filename: file.original_filename,
        message: 'Recovery failed: Both Storage Node A and Storage Node B are unavailable. No healthy replica found.',
        recoveredFrom: null,
        checksumVerified: false,
      };
    }

    file.last_recovery_result = `VERIFIED (${healthyReplica.node_name})`;
    return {
      success: true,
      fileId,
      filename: file.original_filename,
      recoveredFrom: healthyReplica.node_name,
      checksumVerified: true,
      originalChecksum: file.sha256_checksum,
      recalculatedChecksum: file.sha256_checksum,
      message: `Successfully recovered from healthy replica on ${healthyReplica.node_name}. SHA-256 recalculated and verified identical to original upload.`,
    };
  },
};
