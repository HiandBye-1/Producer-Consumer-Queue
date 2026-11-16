
param(
    [string]$Config,
    [string]$Trace
)

if (!$Config -or !$Trace) {
    Write-Host "Usage: .\Run-Checker.ps1 config/file.txt traces/file.csv"
    exit 1
}

$folder = Split-Path $Trace
$name = [System.IO.Path]::GetFileNameWithoutExtension($Trace)

$outputFile = Join-Path $folder "$name-checker-output.txt"
$statusFile = Join-Path $folder "$name-checker-exit-status.txt"

java -cp out checker.TraceChecker $Config $Trace 2>&1 |
    Tee-Object -FilePath $outputFile

$exitCode = $LASTEXITCODE
Set-Content $statusFile $exitCode

Write-Host "Output saved: $outputFile"
Write-Host "Exit status saved: $statusFile"
