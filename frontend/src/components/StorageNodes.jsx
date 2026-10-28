import React from 'react';

export default function StorageNodes({ nodes, onToggleFailure, isSimulating }) {
  const formatTimeAgo = (isoString) => {
    if (!isoString) return 'Never';
    const seconds = Math.floor((Date.now() - new Date(isoString).getTime()) / 1000);
    if (seconds < 5) return 'just now';
    if (seconds < 60) return `${seconds}s ago`;
    return `${Math.floor(seconds / 60)}m ago`;
  };

  return (
    <div className="vs-panel card">
      {/* Panel Header */}
      <div className="vs-panel-header">
        <div className="vs-panel-title">
          {/* Server rack icon */}
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="2" y="2" width="20" height="8" rx="2"></rect>
            <rect x="2" y="14" width="20" height="8" rx="2"></rect>
            <line x1="6" y1="6" x2="6.01" y2="6"></line>
            <line x1="6" y1="18" x2="6.01" y2="18"></line>
          </svg>
          <span>STORAGE NODES</span>
        </div>

        <div className="vs-heartbeat-badge">
          {/* Heartbeat pulse wave icon */}
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
          </svg>
          <span>Heartbeat interval: 5s</span>
        </div>
      </div>

      {/* Two Node Cards Grid */}
      <div className="vs-nodes-grid">
        {nodes.map((node) => {
          const isHealthy = node.status === 'HEALTHY';

          return (
            <div key={node.id} className={`vs-node-card ${isHealthy ? '' : 'vs-node-card-down'}`}>
              {/* Node Card Top */}
              <div className="vs-node-top">
                <div className="vs-node-header-left">
                  <div className="vs-node-avatar">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2">
                      <rect x="2" y="3" width="20" height="8" rx="2"></rect>
                      <rect x="2" y="13" width="20" height="8" rx="2"></rect>
                      <line x1="6" y1="7" x2="6.01" y2="7"></line>
                      <line x1="6" y1="17" x2="6.01" y2="17"></line>
                    </svg>
                  </div>
                  <div>
                    <h3 className="vs-node-name">{node.node_name}</h3>
                    <span className="vs-node-url mono">{node.base_url}</span>
                  </div>
                </div>

                <span className={`vs-node-status-pill ${isHealthy ? 'status-healthy' : 'status-down'}`}>
                  <span className="vs-pill-dot"></span>
                  {node.status}
                </span>
              </div>

              {/* Node Metadata Rows */}
              <div className="vs-node-meta-list">
                <div className="vs-node-meta-row">
                  <div className="vs-meta-label-group">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2">
                      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                    </svg>
                    <span>Storage Directory</span>
                  </div>
                  <span className="vs-meta-value mono">{node.storage_path}</span>
                </div>

                <div className="vs-node-meta-row">
                  <div className="vs-meta-label-group">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2">
                      <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                    </svg>
                    <span>Last Heartbeat</span>
                  </div>
                  <span className="vs-meta-value">{isHealthy ? formatTimeAgo(node.last_heartbeat_at) : 'timeout'}</span>
                </div>

                <div className="vs-node-meta-row">
                  <div className="vs-meta-label-group">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2">
                      <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
                      <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
                      <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
                    </svg>
                    <span>Replicas Stored</span>
                  </div>
                  <span className="vs-meta-value">{node.replica_count || 0}</span>
                </div>
              </div>

              {/* Action Button */}
              <div className="vs-node-action">
                <button
                  className={isHealthy ? 'btn-simulate-fail' : 'btn-simulate-restore'}
                  onClick={() => onToggleFailure(node.id, isHealthy)}
                  disabled={isSimulating}
                  type="button"
                >
                  {isHealthy ? (
                    <>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                        <line x1="12" y1="9" x2="12" y2="13"></line>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                      </svg>
                      Simulate Failure
                    </>
                  ) : (
                    <>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                      Restore Node
                    </>
                  )}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
