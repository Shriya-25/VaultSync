import React, { useState } from 'react';

export default function DemoFlow() {
  const [animKey, setAnimKey] = useState(0);

  const replay = () => {
    setAnimKey((prev) => prev + 1);
  };

  const steps = [
    {
      num: '1',
      title: 'Upload File',
      desc: 'Select a file and calculate SHA-256',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#F1F3EE" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="12" y1="18" x2="12" y2="12"></line>
          <line x1="9" y1="15" x2="15" y2="15"></line>
        </svg>
      ),
    },
    {
      num: '2',
      title: 'Replicate',
      desc: 'Store file on Node A and Node B',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="2" y="3" width="20" height="7" rx="2"></rect>
          <rect x="2" y="14" width="20" height="7" rx="2"></rect>
          <line x1="6" y1="6.5" x2="6.01" y2="6.5"></line>
          <line x1="6" y1="17.5" x2="6.01" y2="17.5"></line>
        </svg>
      ),
    },
    {
      num: '3',
      title: 'Simulate Failure',
      desc: 'Stop heartbeat on one node',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
        </svg>
      ),
    },
    {
      num: '4',
      title: 'Recover & Verify',
      desc: 'Recover from healthy node and verify SHA-256',
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
          <polyline points="9 12 11 14 15 10"></polyline>
        </svg>
      ),
    },
  ];

  return (
    <div className="vs-flow-panel card">
      {/* Panel Header */}
      <div className="vs-panel-header">
        <div className="vs-panel-title vs-flow-title-group">
          {/* Lime Lightning Icon with subtle glow */}
          <div className="vs-flow-icon-pulse">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
            </svg>
          </div>
          <span className="vs-flow-heading">DEMO FLOW</span>
          <span className="vs-flow-sub-text">Run the complete DCS workflow in 4 simple steps.</span>
        </div>

        {/* Replay Animation Trigger */}
        <button
          type="button"
          className="vs-flow-replay-btn"
          onClick={replay}
          title="Click to replay left-to-right flow sequence"
        >
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="1 4 1 10 7 10"></polyline>
            <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
          </svg>
          <span>Replay Flow</span>
        </button>
      </div>

      {/* 4 Step Strip with Sequenced Left-to-Right Animation */}
      <div className="vs-flow-steps-strip" key={animKey}>
        {steps.map((step, idx) => (
          <React.Fragment key={step.num}>
            <div
              className={`vs-step-card vs-step-card-${idx + 1}`}
              style={{
                '--entrance-delay': `${idx * 0.28 + 0.1}s`,
                '--loop-delay': `${idx * 1.5 + 2.2}s`,
              }}
            >
              <div className="vs-step-num-circle">{step.num}</div>
              <div className="vs-step-icon-wrap">{step.icon}</div>
              <div className="vs-step-info">
                <span className="vs-step-title">{step.title}</span>
                <span className="vs-step-desc">{step.desc}</span>
              </div>
            </div>

            {idx < steps.length - 1 && (
              <div
                className={`vs-step-arrow vs-step-arrow-${idx + 1}`}
                style={{
                  '--entrance-delay': `${idx * 0.28 + 0.24}s`,
                  '--loop-delay': `${idx * 1.5 + 2.9}s`,
                }}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                  <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
              </div>
            )}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}
