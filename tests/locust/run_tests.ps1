$ErrorActionPreference = "Stop"

Write-Host "Starting Locust Performance Tests..." -ForegroundColor Cyan

# Check if API Gateway is reachable
$gatewayUrl = "http://localhost:8080/actuator/health" # Assuming actuator is present, or just check port
$port = 8080
$server = "localhost"

$connection = Test-NetConnection -ComputerName $server -Port $port -InformationLevel Quiet

if (-not $connection) {
    Write-Host "API Gateway is NOT reachable at ${server}:${port}. Please start the microservices before running tests." -ForegroundColor Red
    exit 1
}

Write-Host "API Gateway is reachable. Running tests..." -ForegroundColor Green

# Run Locust
# Using --config to load settings from locust.conf
locust --config locust.conf

if ($LASTEXITCODE -eq 0) {
    Write-Host "Tests completed successfully. Report generated at tests/locust/report.html" -ForegroundColor Green
} else {
    Write-Host "Locust tests failed or encountered errors." -ForegroundColor Red
    exit $LASTEXITCODE
}
