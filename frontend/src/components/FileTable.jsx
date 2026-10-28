import React, { useState } from 'react';

export default function FileTable({ files, nodes, onRecover, isRecovering }) {
  const [copiedHash, setCopiedHash] = useState(null);

  const handleCopyHash = (e, hash) => {
    e.stopPropagation();
    navigator.clipboard.writeText(hash);
    setCopiedHash(hash);
    setTimeout(() => setCopiedHash(null), 2000);
  };

  const formatSize = (bytes) => {
    if (!bytes && bytes !== 0) return '0 B';
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const getFileIcon = (filename) => {
    const ext = filename.split('.').pop().toLowerCase();
    if (ext === 'pdf') {
      return (
        <div className="vs-file-icon-badge icon-pdf" title="PDF Document">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#E55353" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
          </svg>
        </div>
      );
    }
    if (ext === 'docx' || ext === 'doc') {
      return (
        <div className="vs-file-icon-badge icon-docx" title="Word Document">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#4C8BF5" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
          </svg>
        </div>
      );
    }
    if (ext === 'png' || ext === 'jpg' || ext === 'svg' || ext === 'webp') {
      return (
        <div className="vs-file-icon-badge icon-img" title="Image File">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#65B987" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <circle cx="8.5" cy="8.5" r="1.5"></circle>
            <polyline points="21 15 16 10 5 21"></polyline>
          </svg>
        </div>
      );
    }
    return (
      <div className="vs-file-icon-badge icon-default" title="File">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2.2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
        </svg>
      </div>
    );
  };

  const healthyNodesCount = nodes.filter((n) => n.status === 'HEALTHY').length;

  return (
    <div className="vs-table-panel card">
      {/* Panel Header */}
      <div className="vs-panel-header">
        <div className="vs-panel-title">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
            <line x1="16" y1="13" x2="8" y2="13"></line>
            <line x1="16" y1="17" x2="8" y2="17"></line>
          </svg>
          <span>BACKED-UP FILES</span>
        </div>
      </div>

      {/* Table */}
      <div className="vs-table-wrap">
        <table className="vs-files-table">
          <thead>
            <tr>
              <th>FILE NAME</th>
              <th>SIZE</th>
              <th>SHA-256 CHECKSUM</th>
              <th>REPLICAS</th>
              <th>RECOVERY STATUS</th>
              <th className="text-right">ACTION</th>
            </tr>
          </thead>
          <tbody>
            {files.length === 0 ? (
              <tr>
                <td colSpan="6" className="vs-empty-td">
                  No backed-up files stored in cluster.
                </td>
              </tr>
            ) : (
              files.map((file) => {
                const shortenedHash = file.sha256_checksum
                  ? `${file.sha256_checksum.substring(0, 16)}..`
                  : 'N/A';

                const isVerified = file.last_recovery_result === 'VERIFIED' || file.last_recovery_result?.includes('VERIFIED');
                const isFailed = file.last_recovery_result?.includes('FAILED');

                return (
                  <tr key={file.id} className="vs-file-row">
                    {/* File Name */}
                    <td className="td-file-name">
                      <div className="vs-name-cell-group">
                        {getFileIcon(file.original_filename)}
                        <span className="vs-row-filename">{file.original_filename}</span>
                      </div>
                    </td>

                    {/* Size */}
                    <td className="td-file-size text-muted">
                      {formatSize(file.size_bytes)}
                    </td>

                    {/* SHA-256 */}
                    <td className="td-file-hash">
                      <div className="vs-hash-pill mono" title={`Full SHA-256: ${file.sha256_checksum}`}>
                        <span>{shortenedHash}</span>
                        <button
                          className="vs-copy-mini-btn"
                          onClick={(e) => handleCopyHash(e, file.sha256_checksum)}
                          title="Copy full SHA-256 digest"
                          type="button"
                        >
                          {copiedHash === file.sha256_checksum ? (
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2.5">
                              <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                          ) : (
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2">
                              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                            </svg>
                          )}
                        </button>
                      </div>
                    </td>

                    {/* Replicas (Node A & Node B) */}
                    <td className="td-file-replicas">
                      <div className="vs-replicas-badges-row">
                        {file.replicas && file.replicas.map((rep, idx) => {
                          const node = nodes.find((n) => n.id === rep.node_id);
                          const isUp = node ? node.status === 'HEALTHY' : true;
                          const nodeLabel = (rep.node_name || rep.node_id || '')
                            .replace('Storage Node ', '')
                            .replace('Node ', '')
                            .replace('node-', '')
                            .toUpperCase();

                          return (
                            <span
                              key={idx}
                              className={`vs-rep-badge ${isUp ? 'vs-rep-up' : 'vs-rep-down'}`}
                              title={`${rep.node_name}: ${isUp ? 'Online' : 'Unavailable'}`}
                            >
                              <span className="vs-rep-dot"></span>
                              <span>{nodeLabel}</span>
                            </span>
                          );
                        })}
                      </div>
                    </td>

                    {/* Recovery Status */}
                    <td className="td-file-status">
                      <span className={`vs-recovery-status-pill ${isVerified ? 'status-verified' : isFailed ? 'status-failed' : 'status-untested'}`}>
                        {file.last_recovery_result || 'UNTESTED'}
                      </span>
                    </td>

                    {/* Action */}
                    <td className="td-file-actions text-right">
                      <div className="vs-action-cluster">
                        <button
                          className="vs-recover-btn"
                          onClick={() => onRecover(file)}
                          disabled={isRecovering}
                          title={healthyNodesCount === 0 ? 'No healthy storage nodes available' : 'Recover replica from healthy node and verify checksum'}
                          type="button"
                        >
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="23 4 23 10 17 10"></polyline>
                            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                          </svg>
                          <span>Recover &amp; Verify</span>
                        </button>

                        <button
                          className="vs-more-btn"
                          onClick={() => onRecover(file)}
                          title="More options"
                          type="button"
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2">
                            <circle cx="12" cy="12" r="1"></circle>
                            <circle cx="12" cy="5" r="1"></circle>
                            <circle cx="12" cy="19" r="1"></circle>
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
