<#
.SYNOPSIS
    VaultSync - One-click Localhost Cluster Startup Script (Windows PowerShell)
.DESCRIPTION
    Starts Storage Node A (8081), Storage Node B (8082), Coordinator (8080),
    and Vite React Frontend (5173) in separate background processes.
#>

$ErrorActionPreference = "Stop"
$rootDir = $PSScriptRoot
$backendDir = Join-Path $rootDir "backend"
$frontendDir = Join-Path $rootDir "frontend"
$jarPath = Join-Path $backendDir "target\vaultsync-backend-1.0.0-SNAPSHOT.jar"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "             VaultSync - Local Cluster Startup              " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Check PostgreSQL on port 5432
Write-Host "[1/5] Checking PostgreSQL database connectivity (port 5432)..." -ForegroundColor Yellow
$pgPort = Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue
if (-not $pgPort) {
    Write-Host "WARNING: No listener found on port 5432. Ensure PostgreSQL 18 is running." -ForegroundColor Red
} else {
    Write-Host "  -> PostgreSQL listener detected on port 5432." -ForegroundColor Green
}

# Resolve DB_PASSWORD from environment if present
if (-not $env:DB_PASSWORD) {
    $userDbPass = [Environment]::GetEnvironmentVariable("DB_PASSWORD", "User")
    if ($userDbPass) {
        $env:DB_PASSWORD = $userDbPass
    } else {
        $sec = Read-Host "  -> Enter PostgreSQL password (leave blank for default 'postgres')" -AsSecureString
        $plain = [System.Net.NetworkCredential]::new("", $sec).Password
        $env:DB_PASSWORD = if ($plain) { $plain } else { "postgres" }
    }
}

# 2. Check executable JAR
if (-not (Test-Path $jarPath)) {
    Write-Host "[2/5] Building backend JAR with Maven..." -ForegroundColor Yellow
    Push-Location $backendDir
    & mvn package -DskipTests
    Pop-Location
} else {
    Write-Host "[2/5] Backend JAR ready: $jarPath" -ForegroundColor Green
}

# 3. Start Storage Node A (Port 8081)
Write-Host "[3/5] Starting Storage Node A on port 8081..." -ForegroundColor Yellow
$nodeAProc = Start-Process java -ArgumentList "-Xms32m", "-Xmx128m", "-jar", "`"$jarPath`"", "--server.port=8081", "--node.id=node-a", "--node.name=`"Node A`"", "--node.dir=`"./storage/node-a`"" -WorkingDirectory $backendDir -PassThru -WindowStyle Hidden
Write-Host "  -> Node A started (PID: $($nodeAProc.Id)) on http://localhost:8081" -ForegroundColor Green

# 4. Start Storage Node B (Port 8082)
Write-Host "[4/5] Starting Storage Node B on port 8082..." -ForegroundColor Yellow
$nodeBProc = Start-Process java -ArgumentList "-Xms32m", "-Xmx128m", "-jar", "`"$jarPath`"", "--server.port=8082", "--node.id=node-b", "--node.name=`"Node B`"", "--node.dir=`"./storage/node-b`"" -WorkingDirectory $backendDir -PassThru -WindowStyle Hidden
Write-Host "  -> Node B started (PID: $($nodeBProc.Id)) on http://localhost:8082" -ForegroundColor Green

# 5. Start Coordinator (Port 8080)
Write-Host "[5/5] Starting Coordinator on port 8080..." -ForegroundColor Yellow
$coordProc = Start-Process java -ArgumentList "-Xms64m", "-Xmx256m", "-jar", "`"$jarPath`"" -WorkingDirectory $backendDir -PassThru -WindowStyle Hidden
Write-Host "  -> Coordinator started (PID: $($coordProc.Id)) on http://localhost:8080" -ForegroundColor Green

# 6. Start Vite Frontend (Port 5173)
Write-Host "[+] Starting Vite Frontend dev server on port 5173..." -ForegroundColor Yellow
$frontendProc = Start-Process cmd -ArgumentList "/c npm run dev -- --host 127.0.0.1 --port 5173" -WorkingDirectory $frontendDir -PassThru -WindowStyle Hidden
Write-Host "  -> Frontend dev server started (PID: $($frontendProc.Id)) on http://localhost:5173" -ForegroundColor Green

Write-Host "`nCluster started successfully! Available services:" -ForegroundColor Cyan
Write-Host "  * React Dashboard:     http://localhost:5173" -ForegroundColor White
Write-Host "  * Coordinator API:     http://localhost:8080/api/health" -ForegroundColor White
Write-Host "  * Storage Node A:      http://localhost:8081/api/health" -ForegroundColor White
Write-Host "  * Storage Node B:      http://localhost:8082/api/health" -ForegroundColor White
Write-Host "  * PostgreSQL Database: localhost:5432/vaultsync`n" -ForegroundColor White
Write-Host "To stop all services, run: .\stop-all.ps1`n" -ForegroundColor Yellow
