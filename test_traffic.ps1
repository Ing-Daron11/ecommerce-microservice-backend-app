$baseUrl = "http://localhost:8080"
$username = "Daron0611"
$password = "Daron0611"

# 1. Create User
$userPayload = @{
    firstName = "Daron"
    lastName = "Garcia"
    imageUrl = "https://example.com/image.jpg"
    email = "test@example.com"
    phone = "1234567890"
    addressDtos = @(
        @{
            fullAddress = "456 Test St"
            postalCode = "54321"
            city = "Test City"
        }
    )
    credential = @{
        username = $username
        password = $password
        roleBasedAuthority = "ROLE_ADMIN"
        isEnabled = $true
        isAccountNonExpired = $true
        isAccountNonLocked = $true
        isCredentialsNonExpired = $true
    }
} | ConvertTo-Json -Depth 10

Write-Host "Creating user..." -ForegroundColor Cyan
try {
    # Changed from /app/api/users to /api/users to hit user-service directly via Gateway
    $createResponse = Invoke-WebRequest -Uri "$baseUrl/api/users" -Method Post -Body $userPayload -ContentType "application/json" -ErrorAction Stop
    Write-Host "User created successfully." -ForegroundColor Green
} catch {
    $errorMessage = $_.Exception.Message
    $responseBody = ""
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $responseBody = $reader.ReadToEnd()
        }
    }
    Write-Host "User creation failed: $errorMessage - Body: $responseBody" -ForegroundColor Yellow
}

# 2. Authenticate
$authPayload = @{
    username = $username
    password = $password
} | ConvertTo-Json

Write-Host "Authenticating..." -ForegroundColor Cyan
try {
    $authResponse = Invoke-WebRequest -Uri "$baseUrl/app/api/authenticate" -Method Post -Body $authPayload -ContentType "application/json" -ErrorAction Stop
    Write-Host "Auth Response Content: $($authResponse.Content)" -ForegroundColor Cyan
    $tokenResponse = $authResponse.Content | ConvertFrom-Json
    $token = $tokenResponse.jwtToken
    Write-Host "Authentication successful. Token received: $token" -ForegroundColor Green
} catch {
    $errorMessage = $_.Exception.Message
    $responseBody = ""
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $responseBody = $reader.ReadToEnd()
        }
    }
    Write-Host "Authentication failed: $errorMessage - Body: $responseBody" -ForegroundColor Red
    exit
}

# 3. Generate Traffic
$headers = @{
    Authorization = "Bearer $token"
}

$endpoints = @(
    "$baseUrl/app/api/products",
    "$baseUrl/app/api/orders",
    "$baseUrl/app/api/shippings",
    "$baseUrl/app/api/users",
    "$baseUrl/app/api/payments",
    "$baseUrl/app/api/favourites"
)

Write-Host "Generating traffic..." -ForegroundColor Cyan
for ($i = 1; $i -le 5; $i++) {
    Write-Host "Round $i..." -ForegroundColor Yellow
    foreach ($url in $endpoints) {
        try {
            $response = Invoke-WebRequest -Uri $url -Method Get -Headers $headers -ErrorAction Stop
            Write-Host "  Success: $url - Status: $($response.StatusCode)" -ForegroundColor Green
        } catch {
             $errorMessage = $_.Exception.Message
             Write-Host "  Failed: $url - Error: $errorMessage" -ForegroundColor Red
        }
    }
    Start-Sleep -Milliseconds 500
}
