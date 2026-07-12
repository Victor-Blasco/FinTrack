# FinTrack Preflight & Validation Script (init.ps1)
# Este script verifica que el entorno este estable antes de que la IA implemente codigo.

if ($null -eq $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
    $env:JAVA_HOME = "C:\Users\Victor\.jdks\temurin-25.0.3"
}

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "[INFO] Iniciando validacion del entorno FinTrack..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Comprobar Docker
Write-Host "1. Verificando estado de Docker..." -ForegroundColor Yellow
$dockerCheck = docker ps 2>$null
if ($null -eq $dockerCheck) {
    Write-Error "Docker no esta iniciado o no esta instalado. Por favor, inicia Docker y vuelve a intentarlo."
    exit 1
}
Write-Host "[OK] Docker esta activo." -ForegroundColor Green

# 2. Verificar contenedores de infraestructura
Write-Host "2. Verificando contenedores de Postgres y Kafka..." -ForegroundColor Yellow
$postgresStatus = docker inspect --format='{{.State.Status}}' fintrack-postgres 2>$null
$kafkaStatus = docker inspect --format='{{.State.Status}}' fintrack-kafka 2>$null

if ($postgresStatus -ne "running" -or $kafkaStatus -ne "running") {
    Write-Warning "Los servicios de infraestructura no estan listos (Postgres: $postgresStatus, Kafka: $kafkaStatus)."
    Write-Host "Intentando levantar la infraestructura con docker-compose up -d..." -ForegroundColor Yellow
    docker-compose up -d
    Start-Sleep -Seconds 5
} else {
    Write-Host "[OK] Contenedores de Postgres y Kafka en ejecucion." -ForegroundColor Green
}

# 3. Comprobar compilacion del backend
Write-Host "3. Verificando compilacion del Backend con Maven..." -ForegroundColor Yellow
$mavenBuild = ./mvnw clean compile -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "La compilacion del backend ha fallado. Revisa los errores antes de continuar."
    exit 1
}
Write-Host "[OK] Backend compila correctamente." -ForegroundColor Green

# 4. Verificar dependencias de Node en el frontend
Write-Host "4. Verificando dependencias del Frontend..." -ForegroundColor Yellow
if (-not (Test-Path "fintrack-web-client/node_modules")) {
    Write-Warning "No se encontro node_modules en fintrack-web-client. Ejecutando npm install..."
    Push-Location fintrack-web-client
    npm install
    Pop-Location
} else {
    Write-Host "[OK] Carpeta node_modules encontrada en el cliente web." -ForegroundColor Green
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "[OK] Entorno validado y estable. Listo para programar!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
exit 0
