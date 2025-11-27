# Configuration
$ACR_NAME = "ecommercestageacr"
$ACR_LOGIN_SERVER = "$ACR_NAME.azurecr.io"
$TAG = "v1.0.0"

# Services list
$services = @(
    "service-discovery",
    "cloud-config",
    "api-gateway",
    "proxy-client",
    "user-service",
    "product-service",
    "order-service",
    "payment-service",
    "shipping-service",
    "favourite-service"
)

# 1. Build JARs
Write-Host "Building JARs with Maven..." -ForegroundColor Cyan
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { 
    Write-Error "Maven build failed!"
    exit 1 
}

# 2. Build and Push Docker Images
foreach ($service in $services) {
    $imageName = "$ACR_LOGIN_SERVER/$service`:$TAG"
    
    Write-Host "Processing $service..." -ForegroundColor Yellow
    
    # Build
    Write-Host "Building Docker image: $imageName"
    docker build -t $imageName -f "$service/Dockerfile" .
    if ($LASTEXITCODE -ne 0) { 
        Write-Error "Docker build failed for $service"
        exit 1 
    }
    
    # Push
    Write-Host "Pushing to ACR: $imageName"
    docker push $imageName
    if ($LASTEXITCODE -ne 0) { 
        Write-Error "Docker push failed for $service"
        exit 1 
    }
}

Write-Host "All images deployed to ACR successfully!" -ForegroundColor Green
