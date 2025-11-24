Write-Host "Building service-discovery..."
minikube image build -t service-discovery:v0.1.0 -f service-discovery/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building cloud-config..."
minikube image build -t cloud-config:v0.1.0 -f cloud-config/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building api-gateway..."
minikube image build -t api-gateway:v0.1.0 -f api-gateway/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building proxy-client..."
minikube image build -t proxy-client:v0.1.0 -f proxy-client/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building user-service..."
minikube image build -t user-service:v0.1.0 -f user-service/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building product-service..."
minikube image build -t product-service:v0.1.0 -f product-service/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building order-service..."
minikube image build -t order-service:v0.1.0 -f order-service/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building payment-service..."
minikube image build -t payment-service:v0.1.0 -f payment-service/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building shipping-service..."
minikube image build -t shipping-service:v0.1.0 -f shipping-service/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Building favourite-service..."
minikube image build -t favourite-service:v0.1.0 -f favourite-service/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "All images built successfully!"
