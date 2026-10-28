import React from 'react';

export default function StatCards({ files, nodes }) {
  const totalFiles = files.length;
  const healthyNodes = nodes.filter((n) => n.status === 'HEALTHY').length;
  const totalNodes = nodes.length;

  return (
    <section className="vs-hero-stats-row">
      {/* Left: Headline & Description */}
      <div className="vs-hero-intro">
        <div className="vs-accent-tick"></div>
        <h1 className="vs-hero-title">
          Backup, <span className="vs-highlight-lime">replicated.</span>
        </h1>
        <p className="vs-hero-desc">
          A small demonstration of fault tolerance and integrity verification.
        </p>
      </div>

      {/* Right: Two Minimal Stat Cards */}
      <div className="vs-stats-duo">
        {/* Card 1: Files Backed Up */}
        <div className="vs-stat-card card">
          <div className="vs-stat-dot-indicator"></div>
          <div className="vs-stat-icon-box">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </div>
          <div className="vs-stat-details">
            <span className="vs-stat-label">FILES BACKED UP</span>
            <span className="vs-stat-number">{totalFiles}</span>
            <span className="vs-stat-sub">Total files stored</span>
          </div>
        </div>

        {/* Card 2: Healthy Nodes */}
        <div className="vs-stat-card card">
          <div className={`vs-stat-dot-indicator ${healthyNodes < totalNodes ? 'dot-warning' : ''}`}></div>
          <div className="vs-stat-icon-box">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="3" width="20" height="7" rx="2"></rect>
              <rect x="2" y="14" width="20" height="7" rx="2"></rect>
              <line x1="6" y1="6.5" x2="6.01" y2="6.5"></line>
              <line x1="6" y1="17.5" x2="6.01" y2="17.5"></line>
            </svg>
          </div>
          <div className="vs-stat-details">
            <span className="vs-stat-label">HEALTHY NODES</span>
            <span className="vs-stat-number">{healthyNodes} / {totalNodes}</span>
            <span className="vs-stat-sub">
              {healthyNodes === totalNodes ? 'All storage nodes online' : '1 storage node offline'}
            </span>
          </div>
        </div>
      </div>
    </section>
  );
}
