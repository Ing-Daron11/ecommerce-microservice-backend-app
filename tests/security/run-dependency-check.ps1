# Script to run OWASP Dependency Check via Maven

Write-Host "Starting OWASP Dependency Check..."

# Run Maven command from the project root
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $ProjectRoot

try {
    ./mvnw dependency-check:check
}
finally {
    Pop-Location
}

Write-Host "Dependency Check complete. Check target/dependency-check-report.html in each module."
