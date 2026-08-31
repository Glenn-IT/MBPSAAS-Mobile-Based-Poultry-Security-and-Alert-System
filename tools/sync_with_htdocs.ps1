# ============================================================
#  MBPSAAS <-> ABMDMS Synchronization Tool (Dynamic Paths)
#  File: tools/sync_with_htdocs.ps1
# ============================================================

param(
    [switch]$CheckOnly,
    [switch]$SyncFromAbmdms,
    [switch]$SyncToAbmdms
)

# Dynamically locate Android Studio Project root from script location (tools\..)
$AnStuDir = Split-Path -Parent $PSScriptRoot
if (-not $AnStuDir -or -not (Test-Path $AnStuDir)) {
    $AnStuDir = (Get-Item -Path $PSScriptRoot).Parent.FullName
}

$AbmdmsDir   = "C:\xampp\htdocs\ABMDMS"
$ApiJunction = "C:\xampp\htdocs\mbpsaas_api"

function Say([string]$text) { Write-Host ("[" + (Get-Date -Format "HH:mm:ss") + "] " + $text) }
function Line() { Write-Host ("=" * 65) }

function Safe-CopyItem([string]$source, [string]$destination, [string]$label) {
    if (-not (Test-Path $source)) {
        Say "   [SKIP] Source not found: $source"
        return
    }
    $targetDir = Split-Path -Parent $destination
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    Copy-Item -Path $source -Destination $destination -Force
    Say "   -> $label"
}

Line
Write-Host "   MBPSAAS <-> ABMDMS Dual-Project Sync Utility"
Line
Write-Host "   ABMDMS Path : $AbmdmsDir"
Write-Host "   Android Path: $AnStuDir"
Write-Host "   API Junction: $ApiJunction"
Line
Write-Host ""

if (-not (Test-Path $AbmdmsDir)) {
    Write-Host "[ERROR] ABMDMS directory not found at $AbmdmsDir" -ForegroundColor Red
    Write-Host "Please ensure XAMPP is installed at C:\xampp\htdocs\ABMDMS"
    exit 1
}

# 1. Check Arduino Sketches
$abmdmsSketch = Join-Path $AbmdmsDir "arduino\motion_sensor\motion_sensor.ino"
$anStuSketch  = Join-Path $AnStuDir "arduino\poultry_sensor\poultry_sensor.ino"

Say "Checking Arduino Sketches..."
if ((Test-Path $abmdmsSketch) -and (Test-Path $anStuSketch)) {
    $abmdmsTime = (Get-Item $abmdmsSketch).LastWriteTime
    $anStuTime  = (Get-Item $anStuSketch).LastWriteTime

    Write-Host "   - ABMDMS Sketch Modified : $abmdmsTime"
    Write-Host "   - Android Sketch Modified: $anStuTime"
} elseif (Test-Path $abmdmsSketch) {
    Write-Host "   - ABMDMS Sketch Found (Android sketch will be created on sync)"
}

# 2. Check Serial Bridges
$abmdmsSerial = Join-Path $AbmdmsDir "serial\serial_reader.ps1"
$anStuSerial  = Join-Path $AnStuDir "serial\serial_reader.ps1"

Say "Checking Serial Bridge Scripts..."
if ((Test-Path $abmdmsSerial) -and (Test-Path $anStuSerial)) {
    $abmdmsSerialTime = (Get-Item $abmdmsSerial).LastWriteTime
    $anStuSerialTime  = (Get-Item $anStuSerial).LastWriteTime

    Write-Host "   - ABMDMS Serial Modified : $abmdmsSerialTime"
    Write-Host "   - Android Serial Modified: $anStuSerialTime"
}

Write-Host ""
if ($SyncFromAbmdms) {
    Say "Synchronizing changes FROM ABMDMS -> Android Project..."

    # Sync Arduino Sketch
    Safe-CopyItem $abmdmsSketch $anStuSketch "Synced Arduino sketch to arduino/poultry_sensor/poultry_sensor.ino"

    # Sync Wiring Docs
    $abmdmsWiring = Join-Path $AbmdmsDir "arduino\PIR_MULTI_ZONE_WIRING.md"
    $anStuWiring  = Join-Path $AnStuDir "docs\WIRING_DIAGRAM.md"
    Safe-CopyItem $abmdmsWiring $anStuWiring "Synced Wiring Diagram to docs/WIRING_DIAGRAM.md"

    # Sync SIM800L Wiring Docs
    $abmdmsSim = Join-Path $AbmdmsDir "arduino\SIM800L_WIRING.md"
    $anStuSim  = Join-Path $AnStuDir "docs\SIM800L_WIRING.md"
    Safe-CopyItem $abmdmsSim $anStuSim "Synced SIM800L Wiring Diagram to docs/SIM800L_WIRING.md"

    Say "Synchronization Complete!" -ForegroundColor Green
}
elseif ($SyncToAbmdms) {
    Say "Synchronizing changes FROM Android Project -> ABMDMS..."

    # Sync Arduino Sketch
    Safe-CopyItem $anStuSketch $abmdmsSketch "Synced poultry_sensor.ino to ABMDMS motion_sensor.ino"

    # Sync API Files to mbpsaas_api junction if present
    $anStuApi = Join-Path $AnStuDir "api"
    if (Test-Path $anStuApi) {
        if (-not (Test-Path $ApiJunction)) {
            New-Item -ItemType Directory -Path $ApiJunction -Force | Out-Null
        }
        Copy-Item -Path (Join-Path $anStuApi "*") -Destination $ApiJunction -Recurse -Force
        Say "   -> Synced API files to $ApiJunction"
    }

    Say "Synchronization Complete!" -ForegroundColor Green
}
else {
    Say "Status Check Complete. Run with -SyncFromAbmdms or -SyncToAbmdms to sync."
}
Line
