<#
Nota: Antes de ejecutar este script se debe hacer una petición post a http://localhost:8080/app/api/users con:
{
  "firstName": "Daron",
  "lastName": "Garcia",
  "imageUrl": "https://example.com/image.jpg",
  "email": "test@example.com",
  "phone": "1234567890",
  "addressDtos": [{
    "fullAddress": "456 Test St",
    "postalCode": "54321",
    "city": "Test City"
  }],
  "credential": {
    "username": "Daron0611",
    "password": "Daron0611",
    "roleBasedAuthority": "ROLE_ADMIN",
    "isEnabled": true,
    "isAccountNonExpired": true,
    "isAccountNonLocked": true,
    "isCredentialsNonExpired": true
  }
}

para crear el usuario, luego se debe hacer login con: http://localhost:8080/app/api/authenticate
{
 "username": "Daron0611",
 "password": "Daron0611"
}

y eso genera un token, dicho token es que se usa para generar el tráfico.
#>

$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXJvbjA2MTEiLCJleHAiOjE3NjQxMjAzNjEsInVzZXJJZCI6IjUiLCJpYXQiOjE3NjQwODQzNjF9.YrtTE7nn4bIlYVupBsPonC76Exz8zSzX7qXxJxkYuCs"
$headers = @{
    Authorization = "Bearer $token"
}

$endpoints = @(
    "http://4.246.232.222:8080/app/api/products",
    "http://4.246.232.222:8080/app/api/orders",
    "http://4.246.232.222:8080/app/api/shippings",
    "http://4.246.232.222:8080/app/api/users",
    "http://4.246.232.222:8080/app/api/payments",
    "http://4.246.232.222:8080/app/api/favourites"
)

Write-Host "Generating traffic for all microservices (5 rounds)..." -ForegroundColor Cyan

for ($i = 1; $i -le 5; $i++) {
    Write-Host "Round $i..." -ForegroundColor Yellow
    foreach ($url in $endpoints) {
        try {
            $response = Invoke-WebRequest -Uri $url -Method Get -Headers $headers -ErrorAction Stop
            Write-Host "  Success: $url - Status: $($response.StatusCode)" -ForegroundColor Green
        } catch {
            Write-Host "  Failed: $url - Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.Exception.Response) {
                 $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
                 $responseBody = $reader.ReadToEnd()
                 Write-Host "  Response Body: $responseBody" -ForegroundColor Yellow
            }
        }
    }
    Start-Sleep -Milliseconds 500
}
Write-Host "Traffic generation complete." -ForegroundColor Cyan
