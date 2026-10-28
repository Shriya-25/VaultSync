import React from 'react';

export default function Header({ isBackendLive, onRefresh, isRefreshing }) {
  return (
    <header className="vs-header">
      <div className="vs-header-left">
        {/* VaultSync Stacked Nodes Logo */}
        <div className="vs-logo-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            {/* Top node pill */}
            <rect x="2" y="4" width="20" height="6.5" rx="3.25" stroke="#B5D94C" strokeWidth="2" fill="none" />
            <line x1="6.5" y1="7.25" x2="6.6" y2="7.25" stroke="#B5D94C" strokeWidth="2.5" strokeLinecap="round" />
            <line x1="10" y1="7.25" x2="10.1" y2="7.25" stroke="#B5D94C" strokeWidth="2.5" strokeLinecap="round" />
            {/* Bottom node pill */}
            <rect x="2" y="13.5" width="20" height="6.5" rx="3.25" stroke="#B5D94C" strokeWidth="2" fill="none" />
            <line x1="6.5" y1="16.75" x2="6.6" y2="16.75" stroke="#B5D94C" strokeWidth="2.5" strokeLinecap="round" />
            <line x1="10" y1="16.75" x2="10.1" y2="16.75" stroke="#B5D94C" strokeWidth="2.5" strokeLinecap="round" />
          </svg>
        </div>

        <div className="vs-brand-text">
          <span className="vs-brand-name">VaultSync</span>
          <span className="vs-header-divider">|</span>
          <span className="vs-header-subtitle">DISTRIBUTED BACKUP / DCS DEMO</span>
        </div>
      </div>

      <div className="vs-header-right">
        <div className="vs-preview-pill" title={isBackendLive ? 'Live backend connected' : 'Phase 1 preview mode with sample data'}>
          <span className="vs-preview-dot"></span>
          <span>{isBackendLive ? 'BACKEND CONNECTED' : 'PHASE 1 · PREVIEW MODE'}</span>
        </div>

        <button
          className="vs-refresh-btn"
          onClick={onRefresh}
          title="Refresh cluster state"
          type="button"
          aria-label="Refresh cluster data"
        >
          <svg className={isRefreshing ? 'spin' : ''} width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
        </button>
      </div>
    </header>
  );
}
