#!/usr/bin/env pwsh

# E2E Testing Script for Azure Pipeline
# Usage: ./run-e2e-tests-simple.ps1 -Environment localhost -BaseUrl http://localhost:8080

param(
    [ValidateSet("localhost", "minikube", "production", "azure")]
    [string]$Environment = "localhost",
    [string]$BaseUrl = "http://localhost:8080"
)

# Force UTF-8 encoding for console output (Windows only)
if ($PSVersionTable.Platform -eq "Win32NT" -or -not $PSVersionTable.Platform) {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
}

$Collection = "./E2E-User-Flows.postman_collection.json"
$ReportDir = Join-Path -Path $PSScriptRoot -ChildPath "reports"
$timestamp = Get-Date -Format "yyyy-MM-dd-HHmmss"

# Create reports directory
if (-not (Test-Path $ReportDir)) {
    New-Item -ItemType Directory -Path $ReportDir -Force | Out-Null
}

# Create temporary environment with custom base URL
$tempEnv = Join-Path -Path $ReportDir -ChildPath "temp-env-$timestamp.json"
$envContent = @{
    id = "e2e-$Environment"
    name = "E2E Testing - $Environment"
    values = @(
        @{ key = "base_url"; value = $BaseUrl; enabled = $true }
        @{ key = "userEmail"; value = ""; enabled = $true }
        @{ key = "userPassword"; value = "TestPassword123!"; enabled = $true }
        @{ key = "authToken"; value = ""; enabled = $true }
        @{ key = "userId"; value = ""; enabled = $true }
        @{ key = "productId"; value = ""; enabled = $true }
        @{ key = "orderId"; value = ""; enabled = $true }
        @{ key = "paymentId"; value = ""; enabled = $true }
    )
    _postman_variable_scope = "environment"
    _postman_exported_at = (Get-Date -Format "o")
    _postman_exported_using = "Newman/latest"
} | ConvertTo-Json -Depth 10

$envContent | Set-Content -Path $tempEnv -Encoding UTF8

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  E2E Tests - $Environment" -ForegroundColor Cyan
Write-Host "  Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# --- WARM-UP LOGIC START ---
Write-Host "Checking service availability (Warm-up)..." -ForegroundColor Yellow
$maxRetries = 30
$retryDelay = 10
$healthCheckUrl = "$BaseUrl/app/api/users"

# JSON Body for Warm-up Request (Create User)
$warmUpBody = @{
    firstName = "WarmUp"
    lastName = "User"
    imageUrl = "https://example.com/image.jpg"
    email = "warmup_$(Get-Random)@example.com"
    phone = "1234567890"
    addressDtos = @(
        @{
            fullAddress = "456 Test St"
            postalCode = "54321"
            city = "Test City"
        }
    )
    credential = @{
        username = "WarmUpUser_$(Get-Random)"
        password = "WarmUpPassword123!"
        roleBasedAuthority = "ROLE_ADMIN"
        isEnabled = $true
        isAccountNonExpired = $true
        isAccountNonLocked = $true
        isCredentialsNonExpired = $true
    }
} | ConvertTo-Json -Depth 5

for ($i = 1; $i -le $maxRetries; $i++) {
    try {
        # Send POST request to trigger repo cloning/initialization
        $response = Invoke-WebRequest -Uri $healthCheckUrl -Method Post -Body $warmUpBody -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
        
        if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
            Write-Host "Service is UP and responding! (Attempt $i/$maxRetries)" -ForegroundColor Green
            break
        }
    }
    catch {
        if ($_.Exception.Response) {
             $statusCode = $_.Exception.Response.StatusCode.value__
             Write-Host "Service returned HTTP $statusCode (Attempt $i/$maxRetries). Waiting ${retryDelay}s..." -ForegroundColor DarkGray
        } else {
             Write-Host "Connection failed (Attempt $i/$maxRetries). Waiting ${retryDelay}s..." -ForegroundColor DarkGray
        }
        Start-Sleep -Seconds $retryDelay
    }
    
    if ($i -eq $maxRetries) {
        Write-Host "WARNING: Service did not respond after warm-up. Proceeding with tests anyway, but they might fail." -ForegroundColor Red
    }
}
# --- WARM-UP LOGIC END ---

$textReport = Join-Path -Path $ReportDir -ChildPath "e2e-results-$Environment-$timestamp.txt"

# Clean up old reports from the same environment
Get-ChildItem -Path $ReportDir -Filter "e2e-results-$Environment-*.txt" | Remove-Item -Force -ErrorAction SilentlyContinue

# Run Newman test with CLI reporter and save output to text file
# Output goes to both console and file simultaneously
newman run $Collection `
  --environment $tempEnv `
  --reporters cli `
  --bail 2>&1 | Tee-Object -FilePath $textReport

# Cleanup
Remove-Item -Path $tempEnv -Force -ErrorAction SilentlyContinue

# Report results
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  ALL TESTS PASSED" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Report saved:" -ForegroundColor Yellow
    Write-Host "  $textReport"
    exit 0
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  TESTS FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Report saved:" -ForegroundColor Yellow
    Write-Host "  $textReport"
    exit 1
}
