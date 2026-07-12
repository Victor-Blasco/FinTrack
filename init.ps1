# FinTrack Preflight & Validation Script (init.ps1)
# Este script verifica que el entorno esté estable antes de que la IA implemente código.

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🔍 Iniciando validación del entorno FinTrack..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Comprobar Docker
Write-Host "1. Verificando estado de Docker..." -ForegroundColor Yellow
$dockerCheck = docker ps 2>$null
if ($null -eq $dockerCheck) {
    Write-Error "Docker no está iniciado o no está instalado. Por favor, inicia Docker y vuelve a intentarlo."
    exit 1
}
Write-Host "✔ Docker está activo." -ForegroundColor Green

# 2. Verificar contenedores de infraestructura
Write-Host "2. Verificando contenedores de Postgres y Kafka..." -ForegroundColor Yellow
$postgresStatus = docker inspect --format='{{.State.Status}}' fintrack-postgres 2>$null
$kafkaStatus = docker inspect --format='{{.State.Status}}' fintrack-kafka 2>$null

if ($postgresStatus -ne "running" -or $kafkaStatus -ne "running") {
    Write-Warning "Los servicios de infraestructura no están listos (Postgres: $postgresStatus, Kafka: $kafkaStatus)."
    Write-Host "Intentando levantar la infraestructura con docker-compose up -d..." -ForegroundColor Yellow
    docker-compose up -d
    Start-Sleep -Seconds 5
} else {
    Write-Host "✔ Contenedores de Postgres y Kafka en ejecución." -ForegroundColor Green
}

# 3. Comprobar compilación del backend
Write-Host "3. Verificando compilación del Backend con Maven..." -ForegroundColor Yellow
$mavenBuild = ./mvnw clean compile -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "La compilación del backend ha fallado. Revisa los errores antes de continuar."
    exit 1
}
Write-Host "✔ Backend compila correctamente." -ForegroundColor Green

# 4. Verificar dependencias de Node en el frontend
Write-Host "4. Verificando dependencias del Frontend..." -ForegroundColor Yellow
if (-not (Test-Path "fintrack-web-client/node_modules")) {
    Write-Warning "No se encontró node_modules en fintrack-web-client. Ejecutando npm install..."
    Push-Location fintrack-web-client
    npm install
    Pop-Location
} else {
    Write-Host "✔ Carpeta node_modules encontrada en el cliente web." -ForegroundColor Green
}

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host "🚀 Entorno validado y estable. ¡Listo para programar!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
exit 0
