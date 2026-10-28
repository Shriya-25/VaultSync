import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import StatCards from './components/StatCards';
import StorageNodes from './components/StorageNodes';
import FileUpload from './components/FileUpload';
import FileTable from './components/FileTable';
import DemoFlow from './components/DemoFlow';
import OperationResult from './components/OperationResult';
import { apiService } from './services/api';

import './App.css';
import './styles/components.css';

export default function App() {
  const [files, setFiles] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isBackendLive, setIsBackendLive] = useState(false);
  const [isSimulating, setIsSimulating] = useState(false);
  const [isRecovering, setIsRecovering] = useState(false);
  const [recoveryResult, setRecoveryResult] = useState(null);

  useEffect(() => {
    loadClusterState();
  }, []);

  const loadClusterState = async () => {
    setIsRefreshing(true);
    try {
      const health = await apiService.checkBackendHealth();
      setIsBackendLive(health.live);

      const [loadedFiles, loadedNodes] = await Promise.all([
        apiService.getFiles(),
        apiService.getNodes(),
      ]);

      setFiles(loadedFiles);
      setNodes(loadedNodes);
    } catch (err) {
      console.error('Error fetching cluster state:', err);
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleToggleFailure = async (nodeId, isCurrentlyHealthy) => {
    setIsSimulating(true);
    try {
      if (isCurrentlyHealthy) {
        await apiService.simulateNodeFailure(nodeId);
      } else {
        await apiService.simulateNodeRecovery(nodeId);
      }
      const updatedNodes = await apiService.getNodes();
      setNodes(updatedNodes);
    } catch (err) {
      console.error('Simulation error:', err);
    } finally {
      setIsSimulating(false);
    }
  };

  const handleUploadSuccess = async (file) => {
    const newRecord = await apiService.uploadFile(file);
    const updatedFiles = await apiService.getFiles();
    const updatedNodes = await apiService.getNodes();
    setFiles(updatedFiles);
    setNodes(updatedNodes);
    return newRecord;
  };

  const handleRecover = async (file) => {
    setIsRecovering(true);
    setRecoveryResult(null);

    try {
      const outcome = await apiService.recoverFile(file.id);
      setRecoveryResult(outcome);
      const updatedFiles = await apiService.getFiles();
      setFiles(updatedFiles);
    } catch (err) {
      setRecoveryResult({
        success: false,
        filename: file.original_filename,
        message: err.message || 'Recovery operation failed unexpectedly.',
      });
    } finally {
      setIsRecovering(false);
    }
  };

  return (
    <div className="vs-app-layout">
      <div className="vs-app-container">
        {/* Top Header */}
        <Header
          isBackendLive={isBackendLive}
          onRefresh={loadClusterState}
          isRefreshing={isRefreshing}
        />

        {/* Hero Section & Minimal Stat Cards */}
        <StatCards
          files={files}
          nodes={nodes}
        />

        {/* Operational Feedback Banner (if recovery triggered) */}
        {recoveryResult && (
          <OperationResult
            result={recoveryResult}
            onDismiss={() => setRecoveryResult(null)}
          />
        )}

        {/* Middle Two-Column Grid: Storage Nodes & Upload & Replicate */}
        <section className="vs-middle-grid">
          <StorageNodes
            nodes={nodes}
            onToggleFailure={handleToggleFailure}
            isSimulating={isSimulating}
          />

          <FileUpload
            onUploadSuccess={handleUploadSuccess}
            nodes={nodes}
          />
        </section>

        {/* Backed-Up Files Table */}
        <FileTable
          files={files}
          nodes={nodes}
          onRecover={handleRecover}
          isRecovering={isRecovering}
        />

        {/* 4-Step Demonstration Flow Strip */}
        <DemoFlow />
      </div>
    </div>
  );
}
