<#
.SYNOPSIS
    VaultSync - Clean Cluster Shutdown Script (Windows PowerShell)
.DESCRIPTION
    Safely finds and terminates only processes listening on ports
    8080 (Coordinator), 8081 (Node A), 8082 (Node B), and 5173 (Frontend).
#>

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "             VaultSync - Stopping Local Cluster             " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$targetPorts = @(8080, 8081, 8082, 5173)

foreach ($port in $targetPorts) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        $pids = $conns.OwningProcess | Select-Object -Unique
        foreach ($pidToKill in $pids) {
            try {
                $proc = Get-Process -Id $pidToKill -ErrorAction SilentlyContinue
                if ($proc) {
                    Write-Host "Stopping process $($proc.ProcessName) (PID: $pidToKill) listening on port $port..." -ForegroundColor Yellow
                    Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
                    Write-Host "  -> Terminated process on port $port." -ForegroundColor Green
                }
            } catch {
                Write-Host "  -> Unable to terminate PID $pidToKill: $_" -ForegroundColor DarkGray
            }
        }
    } else {
        Write-Host "No process listening on port $port." -ForegroundColor DarkGray
    }
}

Write-Host "`nAll VaultSync cluster processes stopped successfully.`n" -ForegroundColor Green
