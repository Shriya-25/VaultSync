import React, { useState } from 'react';

export default function DcsConceptBanner() {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <div className="dcs-banner card">
      <div className="dcs-banner-header" onClick={() => setIsExpanded(!isExpanded)}>
        <div className="dcs-banner-left">
          <div className="dcs-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            DCS Architecture Reference
          </div>
          <span className="dcs-headline">
            Core Distributed Computing Principles Demonstrated: <strong>Replication Quorum</strong>, <strong>Heartbeat Failure Detection</strong> &amp; <strong>SHA-256 Fault-Tolerant Recovery</strong>
          </span>
        </div>
        <button className="dcs-expand-btn" type="button" aria-label="Toggle architecture notes">
          <svg
            className={`arrow-icon ${isExpanded ? 'rotated' : ''}`}
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </button>
      </div>

      {isExpanded && (
        <div className="dcs-banner-body">
          <div className="dcs-pillar-grid">
            <div className="dcs-pillar">
              <div className="pillar-num">01</div>
              <div className="pillar-title">Distributed Storage</div>
              <div className="pillar-desc">
                Physical file copies are stored across isolated node containers/directories (Node Alpha &amp; Node Beta), preventing single-node loss.
              </div>
            </div>

            <div className="dcs-pillar">
              <div className="pillar-num">02</div>
              <div className="pillar-title">File Replication</div>
              <div className="pillar-desc">
                Coordinator replicates uploaded streams to both nodes simultaneously. Marked <code>REPLICATED</code> only after both writes succeed.
              </div>
            </div>

            <div className="dcs-pillar">
              <div className="pillar-num">03</div>
              <div className="pillar-title">Heartbeat Detection</div>
              <div className="pillar-desc">
                Periodic health checks monitor storage nodes. A node is marked <code>UNAVAILABLE</code> if heartbeats miss the configured threshold.
              </div>
            </div>

            <div className="dcs-pillar">
              <div className="pillar-num">04</div>
              <div className="pillar-title">SHA-256 Recovery</div>
              <div className="pillar-desc">
                If Node Alpha fails, the coordinator fails over to Node Beta, computes SHA-256 on recovered bytes, and validates matching checksum.
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
