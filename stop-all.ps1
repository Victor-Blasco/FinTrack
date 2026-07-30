# FinTrack & FraudShield - Stop All Services (stop-all.ps1)
# Detiene los microservicios backend Java y la infraestructura Docker.

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🛑 Deteniendo Ecosistema FinTrack..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Detener procesos Java (Spring Boot)
Write-Host "`n1. Deteniendo microservicios Spring Boot..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
    Write-Host "[OK] Microservicios Java detenidos." -ForegroundColor Green
} else {
    Write-Host "[INFO] No se encontraron procesos Java activos." -ForegroundColor Gray
}

# 2. Detener contenedores Docker
Write-Host "`n2. Deteniendo contenedores Docker (Postgres y Kafka)..." -ForegroundColor Yellow
docker-compose down

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host "✅ Todos los servicios han sido detenidos." -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
