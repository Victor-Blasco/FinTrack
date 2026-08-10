# FinTrack & FraudShield - Start All Services (start-all.ps1)
# Este script levanta la infraestructura, compila los microservicios y arranca todo el ecosistema con un solo comando.

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🚀 Arrancando Ecosistema Completo FinTrack..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Iniciar Infraestructura Docker (Postgres + Kafka)
Write-Host "`n1. Arrancando Infraestructura Docker (PostgreSQL & Kafka)..." -ForegroundColor Yellow

if (Get-Command "docker-compose" -ErrorAction SilentlyContinue) {
    docker-compose up -d
} else {
    docker compose up -d
}

if ($LASTEXITCODE -ne 0) {
    Write-Error "Error iniciando la infraestructura Docker."
    exit 1
}

# 2. Compilar microservicios si no existen los JARs
Write-Host "`n2. Verificando empaquetado de Microservicios Backend (JARs)..." -ForegroundColor Yellow
$services = @("auth-service", "banking-ingest-service", "finance-profile-service", "fraud-detection-service", "categorization-service")
$needsBuild = $false

foreach ($svc in $services) {
    if (-not (Test-Path "$svc/target/$svc-0.0.1-SNAPSHOT.jar")) {
        $needsBuild = $true
        break
    }
}

if ($needsBuild) {
    Write-Host "[INFO] Compilando JARs con Maven..." -ForegroundColor Cyan
    if (Test-Path ".\mvnw.cmd") {
        .\mvnw.cmd clean package -DskipTests
    } else {
        mvn clean package -DskipTests
    }
} else {
    Write-Host "[OK] Todos los artefactos JAR estan listos." -ForegroundColor Green
}

# 3. Arrancar los 5 Microservicios Backend
Write-Host "`n3. Iniciando Microservicios Spring Boot..." -ForegroundColor Yellow

$portMap = @{
    "auth-service" = 8081
    "banking-ingest-service" = 8082
    "finance-profile-service" = 8083
    "fraud-detection-service" = 8084
    "categorization-service" = 8085
}

if (-not (Test-Path "logs")) { New-Item -ItemType Directory -Path "logs" | Out-Null }

foreach ($svc in $services) {
    $port = $portMap[$svc]
    Write-Host " -> Arrancando $svc en puerto $port (logs en logs/$svc.log)..." -ForegroundColor Cyan
    Start-Process -FilePath "java" -ArgumentList "-jar", "$svc/target/$svc-0.0.1-SNAPSHOT.jar", "--server.port=$port" -RedirectStandardOutput "logs/$svc.log" -RedirectStandardError "logs/$svc-error.log" -WindowStyle Hidden
}

# 4. Esperar a que los microservicios estén listos (Health Check)
Write-Host "`n4. Esperando a que los microservicios levanten en sus puertos (8081-8085)..." -ForegroundColor Yellow

foreach ($svc in $services) {
    $port = $portMap[$svc]
    $ready = $false
    $retries = 0
    while (-not $ready -and $retries -lt 30) {
        $connection = Test-NetConnection -ComputerName "localhost" -Port $port -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            $ready = $true
            Write-Host "   [OK] $svc escuchando en puerto $port" -ForegroundColor Green
        } else {
            $retries++
            Start-Sleep -Seconds 1
        }
    }
    if (-not $ready) {
        Write-Host "   [WARN] $svc no respondio a tiempo en puerto $port. Revisa sus logs si falla." -ForegroundColor Red
    }
}

# 5. Arrancar Servidor de Desarrollo del Cliente Web Next.js
Write-Host "`n5. Arrancando Cliente Web (Next.js Dashboard en http://localhost:3000)..." -ForegroundColor Yellow
Push-Location fintrack-web-client
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "npm run dev" -WindowStyle Normal
Pop-Location

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host "✅ Todo el ecosistema FinTrack esta en marcha!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host " * Dashboard Web:      http://localhost:3000" -ForegroundColor White
Write-Host " * Monitor Kafka UI:   http://localhost:8080" -ForegroundColor White
Write-Host " * Microservicios:     Puertos 8081 a 8085" -ForegroundColor White
Write-Host " Para detener todos los procesos ejecuta: .\stop-all.ps1`n" -ForegroundColor Yellow
