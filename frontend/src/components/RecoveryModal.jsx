import React, { useState, useEffect } from 'react';

export default function RecoveryModal({ file, nodes, onClose, onExecuteRecovery }) {
  const [step, setStep] = useState(1); // 1: Analysis, 2: Recovering, 3: Completed
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  // Analyze available replicas vs node health
  const healthyNodes = nodes.filter((n) => n.status === 'HEALTHY');
  const availableReplica = file.replicas.find((r) =>
    healthyNodes.some((hn) => hn.id === r.node_id)
  );

  const unavailableReplicas = file.replicas.filter((r) =>
    !healthyNodes.some((hn) => hn.id === r.node_id)
  );

  const startRecovery = async () => {
    setLoading(true);
    setStep(2);

    try {
      // Allow user to visibly observe DCS recovery steps
      setTimeout(async () => {
        const recoveryOutcome = await onExecuteRecovery(file.id);
        setResult(recoveryOutcome);
        setStep(3);
        setLoading(false);
      }, 1200);
    } catch (err) {
      setResult({
        success: false,
        message: err.message || 'Recovery process encountered an error',
      });
      setStep(3);
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card card recovery-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-group">
            <div className="modal-icon recovery-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#4338ca" strokeWidth="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                <path d="M9 12l2 2 4-4"></path>
              </svg>
            </div>
            <div>
              <h2 className="modal-title">Disaster Recovery &amp; Integrity Verification</h2>
              <span className="modal-subtitle">DCS Fault-Tolerant Retrieval Protocol</span>
            </div>
          </div>

          <button className="btn-icon" onClick={onClose} type="button" title="Close">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div className="modal-body">
          <div className="recovery-file-card">
            <div className="file-info-header">
              <span className="label">Target Backup:</span>
              <strong className="filename">{file.original_filename}</strong>
            </div>
            <div className="file-info-hash">
              <span className="label">Expected SHA-256:</span>
              <code className="mono">{file.sha256_checksum}</code>
            </div>
          </div>

          <div className="dcs-recovery-stepper">
            <div className={`step-item ${step >= 1 ? 'step-active' : ''}`}>
              <div className="step-circle">1</div>
              <div className="step-label">Node Health &amp; Replica Selection</div>
            </div>
            <div className={`step-line ${step >= 2 ? 'line-active' : ''}`}></div>
            <div className={`step-item ${step >= 2 ? 'step-active' : ''}`}>
              <div className="step-circle">2</div>
              <div className="step-label">Replica Streaming</div>
            </div>
            <div className={`step-line ${step >= 3 ? 'line-active' : ''}`}></div>
            <div className={`step-item ${step >= 3 ? 'step-active' : ''}`}>
              <div className="step-circle">3</div>
              <div className="step-label">SHA-256 Integrity Verification</div>
            </div>
          </div>

          {step === 1 && (
            <div className="recovery-analysis">
              <div className="section-title">Replica Availability Analysis</div>

              <div className="analysis-grid">
                {file.replicas.map((rep, idx) => {
                  const node = nodes.find((n) => n.id === rep.node_id);
                  const isNodeHealthy = node && node.status === 'HEALTHY';

                  return (
                    <div key={idx} className={`analysis-card ${isNodeHealthy ? 'card-candidate' : 'card-excluded'}`}>
                      <div className="analysis-card-top">
                        <span className="node-title">{rep.node_name}</span>
                        <span className={`badge ${isNodeHealthy ? 'badge-success' : 'badge-danger'}`}>
                          {isNodeHealthy ? 'HEALTHY' : 'UNAVAILABLE'}
                        </span>
                      </div>
                      <div className="analysis-reason">
                        {isNodeHealthy ? (
                          <span className="text-success">Candidate for recovery failover. Heartbeat active.</span>
                        ) : (
                          <span className="text-danger">Excluded from recovery. Node heartbeat timed out.</span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              {availableReplica ? (
                <div className="recovery-decision-box decision-positive">
                  <div className="decision-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                  <div>
                    <strong>Failover Path Identified:</strong> Coordinator will retrieve replica from{' '}
                    <span className="highlight-node">{availableReplica.node_name}</span>, bypassing any unavailable nodes.
                  </div>
                </div>
              ) : (
                <div className="recovery-decision-box decision-negative">
                  <div className="decision-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="8" x2="12" y2="12"></line>
                      <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                  </div>
                  <div>
                    <strong>Disaster Scenario Detected:</strong> All storage nodes hosting this file are currently unavailable. Recovery cannot proceed until at least one node is restored.
                  </div>
                </div>
              )}
            </div>
          )}

          {step === 2 && (
            <div className="recovery-executing">
              <div className="loader-box">
                <div className="spinner-large"></div>
                <div className="loading-title">Retrieving Bytes &amp; Verifying Cryptographic Digest...</div>
                <div className="loading-desc">
                  Reading replica stream from {availableReplica?.node_name || 'candidate storage node'} and recalculating SHA-256 checksum.
                </div>
              </div>
            </div>
          )}

          {step === 3 && result && (
            <div className="recovery-result">
              {result.success ? (
                <div className="result-success-box">
                  <div className="result-badge-wrap">
                    <div className="result-badge-icon">
                      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                      </svg>
                    </div>
                    <div>
                      <h3 className="result-title text-success">Recovery &amp; Verification Succeeded</h3>
                      <p className="result-subtitle">Content recovered with zero byte corruption.</p>
                    </div>
                  </div>

                  <div className="verification-details-card">
                    <div className="v-row">
                      <span className="v-label">Recovered From:</span>
                      <span className="v-val font-bold">{result.recoveredFromNode}</span>
                    </div>
                    <div className="v-row">
                      <span className="v-label">Stored Original Digest:</span>
                      <code className="v-val mono">{result.originalChecksum}</code>
                    </div>
                    <div className="v-row">
                      <span className="v-label">Recalculated Digest:</span>
                      <code className="v-val mono">{result.calculatedChecksum}</code>
                    </div>
                    <div className="v-row">
                      <span className="v-label">Checksum Match:</span>
                      <span className="badge badge-success">EXACT MATCH (100% INTEGRITY)</span>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="result-failure-box">
                  <div className="result-badge-wrap">
                    <div className="result-badge-icon failure">
                      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="15" y1="9" x2="9" y2="15"></line>
                        <line x1="9" y1="9" x2="15" y2="15"></line>
                      </svg>
                    </div>
                    <div>
                      <h3 className="result-title text-danger">Recovery Failed</h3>
                      <p className="result-subtitle">{result.message}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose} type="button">
            {step === 3 ? 'Done' : 'Cancel'}
          </button>

          {step === 1 && (
            <button
              className="btn btn-primary"
              onClick={startRecovery}
              disabled={!availableReplica || loading}
              type="button"
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="5 3 19 12 5 21 5 3"></polygon>
              </svg>
              Execute Recovery Protocol
            </button>
          )}

          {step === 3 && result && result.success && (
            <button
              className="btn btn-primary"
              onClick={() => {
                alert(`[Phase 1 Demo] File "${file.original_filename}" ready for download. Full byte streaming from Spring Boot backend will execute in Phase 7.`);
                onClose();
              }}
              type="button"
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              Download Verified File
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
