import React from 'react';

export default function OperationResult({ result, onDismiss }) {
  if (!result) return null;

  const isSuccess = result.success;

  return (
    <div className={`vs-result-card card ${isSuccess ? 'res-success-border' : 'res-error-border'}`}>
      <div className="vs-result-top">
        <div className="vs-result-badge-group">
          <div className={`vs-result-icon-box ${isSuccess ? 'icon-success' : 'icon-error'}`}>
            {isSuccess ? (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#65B987" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            ) : (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#D97979" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
              </svg>
            )}
          </div>
          <div>
            <h3 className={`vs-result-title ${isSuccess ? 'text-success' : 'text-error'}`}>
              {isSuccess ? 'Recovery & SHA-256 Verification Succeeded' : 'Disaster Recovery Failed'}
            </h3>
            <span className="vs-result-target">
              Target File: <strong>{result.filename}</strong>
            </span>
          </div>
        </div>

        {onDismiss && (
          <button className="vs-result-dismiss-btn" onClick={onDismiss} type="button" title="Dismiss result">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        )}
      </div>

      <p className="vs-result-message">{result.message}</p>

      {isSuccess && (
        <div className="vs-result-meta-row">
          <div className="vs-res-item">
            <span className="lbl">Failover Source:</span>
            <span className="val text-accent mono font-bold">{result.recoveredFrom}</span>
          </div>
          <div className="vs-res-item">
            <span className="lbl">Integrity Check:</span>
            <span className="vs-tag-matched">SHA-256 MATCH (100% INTEGRITY)</span>
          </div>
          <div className="vs-res-item hash-full-row">
            <span className="lbl">Verified Digest:</span>
            <code className="val mono hash-text">{result.recalculatedChecksum}</code>
          </div>
        </div>
      )}
    </div>
  );
}
