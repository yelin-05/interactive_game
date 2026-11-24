param(
    [string]$BaseUrl = 'http://127.0.0.1:18080/moshi'
)

$ErrorActionPreference = 'Stop'
$url = $BaseUrl.TrimEnd('/') + '/health'
$body = Join-Path ([System.IO.Path]::GetTempPath()) ("moshi-health-" + [guid]::NewGuid() + ".json")

try {
    $response = Invoke-WebRequest -Uri $url -TimeoutSec 10 -OutFile $body -PassThru
    $status = [int]$response.StatusCode
    Write-Host "HTTP $status  $url"
    Get-Content -LiteralPath $body -Raw
    if ($status -ne 200) { throw "Expected HTTP 200, got $status" }
    $json = Get-Content -LiteralPath $body -Raw | ConvertFrom-Json
    if ($json.status -ne 'UP' -or $json.db -ne 'UP') { throw 'Health payload is not UP' }
    Write-Host 'PASS: application and database are healthy'
}
catch {
    Write-Error $_
    exit 1
}
finally {
    Remove-Item -LiteralPath $body -ErrorAction SilentlyContinue
}
