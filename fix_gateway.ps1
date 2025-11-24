Write-Host "Rebuilding API Gateway JAR..."
mvn clean package -DskipTests -pl api-gateway -am
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Rebuilding API Gateway Docker Image..."
minikube image build -t api-gateway:v0.1.0 -f api-gateway/Dockerfile .
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Restarting API Gateway Pod..."
kubectl delete pod -l app=api-gateway

Write-Host "Done. Wait for pod to be ready."
