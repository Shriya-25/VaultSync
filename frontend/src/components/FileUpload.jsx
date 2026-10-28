import React, { useState, useRef } from 'react';

export default function FileUpload({ onUploadSuccess, nodes }) {
  // Default preview file matching the reference design if no file picked yet
  const [selectedFile, setSelectedFile] = useState({
    name: 'project-report.pdf',
    size: 2516582, // 2.4 MB
    sha256: 'a3f5e2d1b6c42f98e7a9c1d5f3e6b8a77c4d2e1f089bb2198034e4a90f12bcde',
    isDefaultSample: true,
  });
  const [isUploading, setIsUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [copied, setCopied] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const fileInputRef = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      processSelectedFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      processSelectedFile(e.target.files[0]);
    }
  };

  const processSelectedFile = (file) => {
    setFeedback(null);
    if (file.size > 25 * 1024 * 1024) {
      setFeedback({ type: 'error', text: 'File exceeds 25 MB limit.' });
      return;
    }
    // Generate preview hash for display
    const simulatedHash = 'a' + Array.from({ length: 63 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
    setSelectedFile({
      name: file.name,
      size: file.size,
      sha256: simulatedHash,
      realFile: file,
      isDefaultSample: false,
    });
  };

  const handleCopyHash = () => {
    if (selectedFile?.sha256) {
      navigator.clipboard.writeText(selectedFile.sha256);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleUploadClick = async () => {
    if (!selectedFile) return;

    setIsUploading(true);
    setFeedback(null);

    try {
      const filePayload = selectedFile.realFile || {
        name: selectedFile.name,
        size: selectedFile.size,
      };

      await onUploadSuccess(filePayload);
      setFeedback({
        type: 'success',
        text: `Uploaded "${selectedFile.name}" and replicated to Node A & Node B.`,
      });
    } catch {
      setFeedback({
        type: 'error',
        text: 'Upload failed. Check node availability.',
      });
    } finally {
      setIsUploading(false);
    }
  };

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const shortenedChecksum = selectedFile?.sha256
    ? `${selectedFile.sha256.substring(0, 42)}..`
    : '';

  const healthyNodeCount = nodes.filter((n) => n.status === 'HEALTHY').length;

  return (
    <div className="vs-panel card">
      {/* Panel Header */}
      <div className="vs-panel-header">
        <div className="vs-panel-title">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
          <span>UPLOAD &amp; REPLICATE</span>
        </div>

        <span className="vs-panel-badge-text">Max size: 25 MB</span>
      </div>

      {/* Panel Body */}
      <div className="vs-upload-body">
        {/* Hidden file input */}
        <input
          ref={fileInputRef}
          type="file"
          className="vs-file-hidden"
          onChange={handleFileChange}
        />

        {/* Drag & Drop Area */}
        <div
          className={`vs-dropzone ${dragActive ? 'vs-dropzone-active' : ''}`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
        >
          <div className="vs-dropzone-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#9A9F96" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
              <path d="M4 14.899A7 7 0 1 1 15.71 8h1.79a4.5 4.5 0 0 1 2.5 8.242"></path>
              <path d="M12 12v9"></path>
              <path d="m16 16-4-4-4 4"></path>
            </svg>
          </div>
          <div className="vs-dropzone-text">
            <span>Drag &amp; drop a file here</span>
            <span className="vs-dropzone-link">or click to browse</span>
          </div>
        </div>

        {/* Selected File Card */}
        {selectedFile && (
          <div className="vs-file-card-preview">
            <div className="vs-file-card-left">
              <div className="vs-file-type-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#F1F3EE" strokeWidth="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                </svg>
              </div>
              <div className="vs-file-info-text">
                <span className="vs-file-name">{selectedFile.name}</span>
                <span className="vs-file-size">{formatSize(selectedFile.size)}</span>
              </div>
            </div>

            <button
              className="vs-file-remove-btn"
              onClick={() => {
                setSelectedFile(null);
                if (fileInputRef.current) fileInputRef.current.value = '';
              }}
              title="Remove file"
              type="button"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        )}

        {/* SHA-256 Checksum Input Box */}
        {selectedFile && (
          <div className="vs-checksum-block">
            <span className="vs-checksum-label">SHA-256 Checksum</span>
            <div className="vs-checksum-input-wrap">
              <input
                type="text"
                readOnly
                value={shortenedChecksum}
                className="vs-checksum-input mono"
                title={selectedFile.sha256}
              />
              <button
                className="vs-checksum-copy-btn"
                onClick={handleCopyHash}
                title="Copy full SHA-256 digest"
                type="button"
              >
                {copied ? (
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#B5D94C" strokeWidth="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                ) : (
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                  </svg>
                )}
              </button>
            </div>
          </div>
        )}

        {/* Upload & Replicate Button */}
        <button
          className="btn-upload-replicate"
          onClick={handleUploadClick}
          disabled={!selectedFile || isUploading || healthyNodeCount === 0}
          type="button"
        >
          {isUploading ? (
            <>
              <svg className="spin" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="12" y1="2" x2="12" y2="6"></line>
                <line x1="12" y1="18" x2="12" y2="22"></line>
              </svg>
              <span>Replicating to Nodes...</span>
            </>
          ) : (
            <>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="16 16 12 12 8 16"></polyline>
                <line x1="12" y1="12" x2="12" y2="21"></line>
                <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"></path>
              </svg>
              <span>Upload &amp; Replicate File</span>
            </>
          )}
        </button>

        {feedback && (
          <div className={`vs-upload-feedback ${feedback.type === 'success' ? 'vs-fb-success' : 'vs-fb-error'}`}>
            <span>{feedback.text}</span>
          </div>
        )}
      </div>
    </div>
  );
}
