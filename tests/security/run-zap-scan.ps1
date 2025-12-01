# Script to run OWASP ZAP Baseline Scan against the application

param (
    [string]$TargetUrl = "http://host.docker.internal:8900/app",
    [string]$ReportName = "zap-report.html"
)

Write-Host "Starting OWASP ZAP Baseline Scan against $TargetUrl..."

# Ensure the reports directory exists
$ReportDir = Join-Path $PSScriptRoot "reports"
if (-not (Test-Path $ReportDir)) {
    New-Item -ItemType Directory -Path $ReportDir | Out-Null
}

# Run ZAP in Docker
# -t: Target URL
# -r: Report file name
# -I: Fail on warnings (optional, removed for now)
# -j: Generate JSON report (optional)
# -d: Show debug messages
docker run --rm -v "$ReportDir`:/zap/wrk/:rw" -t zaproxy/zap-stable zap-baseline.py -t $TargetUrl -r $ReportName

Write-Host "Scan complete. Report saved to $ReportDir\$ReportName"
