# Script de verificacion de microservicios
Write-Host "========================================"
Write-Host "  VERIFICACION DE MICROSERVICIOS"
Write-Host "========================================"
Write-Host ""

# Verificar contenedores Docker
Write-Host "1. Estado de Contenedores Docker:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host ""

# Verificar endpoints de health
Write-Host "2. Health Checks de Servicios:"

$services = @(
    @{Name="Zipkin"; URL="http://localhost:9411/health"}
    @{Name="Eureka"; URL="http://localhost:8761/actuator/health"}
    @{Name="Config Server"; URL="http://localhost:9296/actuator/health"}
    @{Name="API Gateway"; URL="http://localhost:8080/actuator/health"}
    @{Name="Proxy Client"; URL="http://localhost:8900/actuator/health"}
    @{Name="User Service"; URL="http://localhost:8700/user-service/actuator/health"}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri $service.URL -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        Write-Host "  [OK] $($service.Name) - Status: $($response.StatusCode)"
    }
    catch {
        Write-Host "  [FAIL] $($service.Name) - No disponible"
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "  URLs de Acceso:"
Write-Host "========================================"
Write-Host "  Eureka Dashboard:  http://localhost:8761"
Write-Host "  Zipkin UI:         http://localhost:9411"
Write-Host "  API Gateway:       http://localhost:8080"
Write-Host "  User Service:      http://localhost:8700/user-service"
Write-Host "========================================"
