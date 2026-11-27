param([string]$InstallDevice = '')
$ErrorActionPreference = 'Stop'
$taskProject = Split-Path -Parent $PSScriptRoot
$taskWorkspace = Split-Path -Parent (Split-Path -Parent $taskProject)
$taskEnvironment = Join-Path $taskWorkspace '.tools\pico-env.ps1'
if (Test-Path -LiteralPath $taskEnvironment) { . $taskEnvironment }

# Android/Gradle workers on Windows can fail with non-ASCII classpaths.
# Junctions keep the real checkout in place; no user files are moved or removed.
function Get-AsciiAlias([string]$Target, [string]$Name) {
    if ($Target -notmatch '[^\x00-\x7F]') { return $Target }
    $taskAlias = Join-Path $env:TEMP $Name
    if (Test-Path -LiteralPath $taskAlias) {
        if ((Get-Item -LiteralPath $taskAlias).Target -eq $Target) { return $taskAlias }
        $taskHash = [Convert]::ToHexString([System.Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($Target))).Substring(0, 10)
        $taskAlias = Join-Path $env:TEMP ($Name + '-' + $taskHash)
    }
    if (-not (Test-Path -LiteralPath $taskAlias)) { New-Item -ItemType Junction -Path $taskAlias -Target $Target | Out-Null }
    if ((Get-Item -LiteralPath $taskAlias).Target -ne $Target) { throw "Temporary alias points elsewhere: $taskAlias" }
    return $taskAlias
}
$taskBuildRoot = Get-AsciiAlias $taskProject 'colab-spatial-project'
if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME = Get-AsciiAlias $env:GRADLE_USER_HOME 'colab-gradle-home' }
& (Join-Path $taskBuildRoot 'gradlew.bat') -p $taskBuildRoot :app:assembleDebug :app:testDebugUnitTest --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed; see Gradle output above.' }
$taskApk = Join-Path $taskProject 'app\build\outputs\apk\debug\app-debug.apk'
Write-Output "APK: $taskApk"
if ($InstallDevice) {
    if (-not $taskPicoCli) { $taskPicoCli = (Get-Command pico-cli -ErrorAction Stop).Source }
    & $taskPicoCli app install $taskApk --device $InstallDevice
    if ($LASTEXITCODE -ne 0) { throw 'APK installation failed.' }
    & $taskPicoCli app launch com.colab.spatial --device $InstallDevice
    if ($LASTEXITCODE -ne 0) { throw 'App launch failed.' }
}
