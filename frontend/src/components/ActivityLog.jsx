import React from 'react';

export default function ActivityLog({ operations, onRefresh }) {
  const getOpBadge = (type, status) => {
    const isSuccess = status === 'SUCCESS';
    switch (type) {
      case 'UPLOAD':
        return <span className="badge badge-indigo">Upload</span>;
      case 'REPLICATION':
        return <span className="badge badge-info">Replication</span>;
      case 'HEARTBEAT':
        return isSuccess ? (
          <span className="badge badge-success">Heartbeat</span>
        ) : (
          <span className="badge badge-danger">Heartbeat Lost</span>
        );
      case 'RECOVERY':
        return isSuccess ? (
          <span className="badge badge-success">DR Failover</span>
        ) : (
          <span className="badge badge-danger">DR Failed</span>
        );
      case 'VERIFICATION':
        return <span className="badge badge-success">SHA-256 Match</span>;
      default:
        return <span className="badge badge-indigo">{type}</span>;
    }
  };

  const formatLogTime = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  return (
    <div className="activity-log-section card">
      <div className="card-header">
        <div>
          <div className="card-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="8" y1="6" x2="21" y2="6"></line>
              <line x1="8" y1="12" x2="21" y2="12"></line>
              <line x1="8" y1="18" x2="21" y2="18"></line>
              <line x1="3" y1="6" x2="3.01" y2="6"></line>
              <line x1="3" y1="12" x2="3.01" y2="12"></line>
              <line x1="3" y1="18" x2="3.01" y2="18"></line>
            </svg>
            Operation History &amp; Audit Trail ({operations.length})
          </div>
          <p className="card-subtitle">
            Records events for upload, replica distribution, heartbeat checks, and disaster recoveries.
          </p>
        </div>

        <button className="btn btn-sm btn-secondary" onClick={onRefresh} type="button">
          Refresh Logs
        </button>
      </div>

      <div className="card-body p-0">
        <div className="log-list">
          {operations.length === 0 ? (
            <div className="log-empty text-muted">No logged operations yet.</div>
          ) : (
            operations.map((op) => (
              <div key={op.id} className="log-item">
                <div className="log-time-col">
                  <span className="log-time mono">{formatLogTime(op.timestamp)}</span>
                </div>

                <div className="log-badge-col">
                  {getOpBadge(op.operation_type, op.status)}
                </div>

                <div className="log-details-col">
                  <div className="log-message">{op.message}</div>
                  {op.target && <div className="log-target mono text-muted">Target: {op.target}</div>}
                </div>

                <div className="log-status-col">
                  <span className={`status-indicator ${op.status === 'SUCCESS' ? 'text-success' : 'text-danger'}`}>
                    {op.status}
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
