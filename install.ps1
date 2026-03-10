$ErrorActionPreference = "Stop"

$repo = "Atlantis-Services/envmaster-cli"
$installDir = "$env:LOCALAPPDATA\envmaster"
$binPath = "$installDir\envmaster.exe"

# Get latest version
$release = Invoke-RestMethod "https://api.github.com/repos/$repo/releases/latest"
$version = $release.tag_name
$url = "https://github.com/$repo/releases/download/$version/envmaster-windows-x64.exe"

Write-Host "Installing envmaster $version..."

New-Item -ItemType Directory -Force -Path $installDir | Out-Null
Invoke-WebRequest $url -OutFile $binPath

# Add to user PATH if not already present
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
if ($currentPath -notlike "*$installDir*") {
    [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$installDir", "User")
    Write-Host "Added $installDir to PATH."
}

Write-Host ""
Write-Host "Installed to $binPath"
Write-Host "Restart your terminal and run 'envmaster --help' to get started."