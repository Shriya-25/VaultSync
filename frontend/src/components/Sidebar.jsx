import React from 'react';

export default function Sidebar({ activeTab, setActiveTab, nodes }) {
  const healthyCount = nodes.filter((n) => n.status === 'HEALTHY').length;

  const navItems = [
    {
      id: 'dashboard',
      label: 'Cluster Overview',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="3" width="7" height="7"></rect>
          <rect x="14" y="3" width="7" height="7"></rect>
          <rect x="14" y="14" width="7" height="7"></rect>
          <rect x="3" y="14" width="7" height="7"></rect>
        </svg>
      ),
    },
    {
      id: 'files',
      label: 'Backed-Up Files',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
          <polyline points="10 9 9 9 8 9"></polyline>
        </svg>
      ),
    },
    {
      id: 'nodes',
      label: 'Storage Nodes',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect>
          <rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect>
          <line x1="6" y1="6" x2="6.01" y2="6"></line>
          <line x1="6" y1="18" x2="6.01" y2="18"></line>
        </svg>
      ),
    },
    {
      id: 'upload',
      label: 'Upload New File',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="17 8 12 3 7 8"></polyline>
          <line x1="12" y1="3" x2="12" y2="15"></line>
        </svg>
      ),
    },
    {
      id: 'operations',
      label: 'Operation Logs',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <line x1="8" y1="6" x2="21" y2="6"></line>
          <line x1="8" y1="12" x2="21" y2="12"></line>
          <line x1="8" y1="18" x2="21" y2="18"></line>
          <line x1="3" y1="6" x2="3.01" y2="6"></line>
          <line x1="3" y1="12" x2="3.01" y2="12"></line>
          <line x1="3" y1="18" x2="3.01" y2="18"></line>
        </svg>
      ),
    },
  ];

  return (
    <aside className="app-sidebar">
      <div className="sidebar-brand">
        <div className="brand-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="10" rx="2"></rect>
            <circle cx="12" cy="16" r="1.5"></circle>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
        </div>
        <div className="brand-text">
          <span className="brand-name">VaultSync</span>
          <span className="brand-subtitle">Distributed Backup &amp; DR</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-section-title">NAVIGATION</div>
        {navItems.map((item) => (
          <button
            key={item.id}
            className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
            onClick={() => setActiveTab(item.id)}
            type="button"
          >
            <span className="nav-item-icon">{item.icon}</span>
            <span className="nav-item-label">{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="cluster-health-widget">
          <div className="widget-header">
            <span className="widget-title">Cluster Status</span>
            <span className={`status-dot ${healthyCount === nodes.length ? 'dot-healthy' : 'dot-degraded'}`}></span>
          </div>
          <div className="widget-metrics">
            <div className="metric-row">
              <span className="metric-label">Nodes Healthy:</span>
              <span className="metric-val">{healthyCount} / {nodes.length}</span>
            </div>
            <div className="metric-row">
              <span className="metric-label">Replication Factor:</span>
              <span className="metric-val">2x (Quorum)</span>
            </div>
            <div className="metric-row">
              <span className="metric-label">Integrity Mode:</span>
              <span className="metric-val">SHA-256</span>
            </div>
          </div>
        </div>

        <div className="academic-tag">
          <span>DCS Mini-Project</span>
          <span className="tag-version">Phase 1 UI</span>
        </div>
      </div>
    </aside>
  );
}
