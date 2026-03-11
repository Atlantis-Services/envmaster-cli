$ErrorActionPreference = "Stop"

$repo = "Atlantis-Services/envmaster-cli"
$installDir = "$env:LOCALAPPDATA\envmaster"
$binPath = "$installDir\envmaster.exe"
$tempPath = "$env:TEMP\envmaster_new.exe"

$release = Invoke-RestMethod "https://api.github.com/repos/$repo/releases/latest"
$version = $release.tag_name
$url = "https://github.com/$repo/releases/download/$version/envmaster-windows-x64.exe"

Write-Host "Installing envmaster $version..."

New-Item -ItemType Directory -Force -Path $installDir | Out-Null

Invoke-WebRequest $url -OutFile $tempPath

if (Test-Path $binPath) {
    try {
        Move-Item -Force $tempPath $binPath
    } catch {
        $bat = "$env:TEMP\envmaster_update.bat"
        @"
@echo off
ping 127.0.0.1 -n 3 > nul
move /y "$tempPath" "$binPath"
del /f /q "%~f0"
"@ | Set-Content $bat
        Start-Process "cmd" -ArgumentList "/c", $bat -WindowStyle Hidden
        Write-Host ""
        Write-Host "envmaster is currently running. It will be updated automatically in a few seconds."
        Write-Host "Restart your terminal after."
        exit 0
    }
} else {
    Move-Item $tempPath $binPath
}

$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
if ($currentPath -notlike "*$installDir*") {
    [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$installDir", "User")
    Write-Host "Added $installDir to PATH."
}

Write-Host ""
Write-Host "Installed to $binPath"
Write-Host "Restart your terminal and run 'envmaster --help' to get started."