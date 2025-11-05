# Script para generar trafico completo en los microservicios

$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXJvbjA2MTEiLCJleHAiOjE3NjIzOTU4MjgsInVzZXJJZCI6IjUiLCJpYXQiOjE3NjIzNTk4Mjh9.cLoXI-UooQawfi6FCTfYYCoJLWZfOB1zIikv36SFXyc"
$apiGatewayUrl = "http://localhost:30080"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "=== Generando trafico completo en microservicios ===" -ForegroundColor Green
Write-Host ""

# Hacer multiples llamadas para activar todas las trazas
for ($i = 1; $i -le 3; $i++) {
    Write-Host "Iteracion $i..." -ForegroundColor Yellow
    
    # 1. Product Service
    Write-Host "  1. Productos..." -ForegroundColor Gray
    try {
        Invoke-RestMethod -Uri "$apiGatewayUrl/product-service/api/products" `
            -Headers $headers -Method Get -ErrorAction SilentlyContinue | Out-Null
    } catch { }
    
    # 2. Favourite Service
    Write-Host "  2. Favoritos..." -ForegroundColor Gray
    try {
        Invoke-RestMethod -Uri "$apiGatewayUrl/favourite-service/api/favourites" `
            -Headers $headers -Method Get -ErrorAction SilentlyContinue | Out-Null
    } catch { }
    
    # 3. User Service
    Write-Host "  3. Usuario..." -ForegroundColor Gray
    try {
        Invoke-RestMethod -Uri "$apiGatewayUrl/user-service/api/users/5" `
            -Headers $headers -Method Get -ErrorAction SilentlyContinue | Out-Null
    } catch { }
    
    # 4. Order Service
    Write-Host "  4. Ordenes..." -ForegroundColor Gray
    try {
        Invoke-RestMethod -Uri "$apiGatewayUrl/order-service/api/orders" `
            -Headers $headers -Method Get -ErrorAction SilentlyContinue | Out-Null
    } catch { }
    
    # 5. Shipping Service
    Write-Host "  5. Envios..." -ForegroundColor Gray
    try {
        Invoke-RestMethod -Uri "$apiGatewayUrl/shipping-service/api/shipments" `
            -Headers $headers -Method Get -ErrorAction SilentlyContinue | Out-Null
    } catch { }
    
    # 6. Payment Service
    Write-Host "  6. Pagos..." -ForegroundColor Gray
    try {
        Invoke-RestMethod -Uri "$apiGatewayUrl/payment-service/api/payments" `
            -Headers $headers -Method Get -ErrorAction SilentlyContinue | Out-Null
    } catch { }
    
    Start-Sleep -Seconds 1
}

Write-Host ""
Write-Host "=== Trafico generado ===" -ForegroundColor Green
Write-Host "Ve a Zipkin: http://localhost:9411" -ForegroundColor Yellow


