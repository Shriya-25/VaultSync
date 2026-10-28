import React, { useState } from 'react';

export default function FileDetailModal({ file, nodes, onClose, onRecover }) {
  const [copied, setCopied] = useState(false);

  if (!file) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(file.sha256_checksum);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const formatBytes = (bytes) => {
    if (!bytes && bytes !== 0) return '0 B';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-group">
            <div className="modal-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#4338ca" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
              </svg>
            </div>
            <div>
              <h2 className="modal-title">{file.original_filename}</h2>
              <span className="modal-subtitle mono">ID: {file.id}</span>
            </div>
          </div>

          <button className="btn-icon" onClick={onClose} type="button" title="Close modal">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div className="modal-body">
          <div className="metadata-summary-grid">
            <div className="meta-item">
              <span className="meta-label">File Size</span>
              <span className="meta-value">{formatBytes(file.size_bytes)} ({file.size_bytes.toLocaleString()} bytes)</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">MIME Type</span>
              <span className="meta-value mono">{file.content_type || 'application/octet-stream'}</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Backup Status</span>
              <span className="meta-value">
                <span className="badge badge-success">{file.backup_status}</span>
              </span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Upload Timestamp</span>
              <span className="meta-value">{new Date(file.created_at).toLocaleString()}</span>
            </div>
          </div>

          <div className="hash-section">
            <div className="hash-header">
              <span className="hash-title">SHA-256 Authoritative Checksum</span>
              <button className="btn btn-sm btn-secondary" onClick={handleCopy} type="button">
                {copied ? 'Copied!' : 'Copy Hash'}
              </button>
            </div>
            <div className="hash-block">
              <code>{file.sha256_checksum}</code>
            </div>
            <span className="hash-hint">
              Computed directly from uploaded byte stream before distributed write. Used during recovery for integrity validation.
            </span>
          </div>

          <div className="replicas-section">
            <div className="section-title">Physical Replica Locations</div>
            <div className="replica-cards-list">
              {file.replicas && file.replicas.map((rep, idx) => {
                const nodeInfo = nodes.find((n) => n.id === rep.node_id);
                const isNodeUp = nodeInfo ? nodeInfo.status === 'HEALTHY' : true;

                return (
                  <div key={idx} className={`replica-detail-card ${isNodeUp ? 'node-up' : 'node-down'}`}>
                    <div className="rep-card-top">
                      <div className="rep-node-info">
                        <span className="rep-node-name">{rep.node_name}</span>
                        <span className={`badge ${isNodeUp ? 'badge-success' : 'badge-danger'}`}>
                          {isNodeUp ? 'REACHABLE' : 'UNAVAILABLE'}
                        </span>
                      </div>
                      <span className="rep-status-badge badge badge-indigo">{rep.status}</span>
                    </div>

                    <div className="rep-storage-key">
                      <span className="label">Storage Key:</span>
                      <code className="key-code">{rep.storage_key}</code>
                    </div>

                    <div className="rep-node-meta">
                      <span>Node Base: {nodeInfo?.base_url || 'Internal cluster'}</span>
                      <span>Directory: {nodeInfo?.storage_path || 'Isolated volume'}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose} type="button">
            Close
          </button>
          <button
            className="btn btn-primary"
            onClick={() => {
              onClose();
              onRecover(file);
            }}
            type="button"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
            </svg>
            Trigger Disaster Recovery Test
          </button>
        </div>
      </div>
    </div>
  );
}
