# ============================================================
#  MBPSAAS <-> ABMDMS Synchronization Tool
#  File: tools/sync_with_htdocs.ps1
# ============================================================

param(
    [switch]$CheckOnly,
    [switch]$SyncFromAbmdms,
    [switch]$SyncToAbmdms
)

$AbmdmsDir = "C:\xampp\htdocs\ABMDMS"
$AnStuDir  = "C:\Users\GLENN\AndroidStudioProjects\MBPSAASMobileBasedPoultrySecurityandAlertSystem"
$ApiJunction = "C:\xampp\htdocs\mbpsaas_api"

function Say([string]$text) { Write-Host ("[" + (Get-Date -Format "HH:mm:ss") + "] " + $text) }
function Line() { Write-Host ("=" * 65) }

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
    if (Test-Path $abmdmsSketch) {
        Copy-Item $abmdmsSketch $anStuSketch -Force
        Say "   -> Synced Arduino sketch to arduino/poultry_sensor/poultry_sensor.ino"
    }

    # Sync Wiring Docs
    if (Test-Path (Join-Path $AbmdmsDir "arduino\PIR_MULTI_ZONE_WIRING.md")) {
        Copy-Item (Join-Path $AbmdmsDir "arduino\PIR_MULTI_ZONE_WIRING.md") (Join-Path $AnStuDir "docs\WIRING_DIAGRAM.md") -Force
        Say "   -> Synced Wiring Diagram to docs/WIRING_DIAGRAM.md"
    }

    Say "Synchronization Complete!" -ForegroundColor Green
}
elseif ($SyncToAbmdms) {
    Say "Synchronizing changes FROM Android Project -> ABMDMS..."

    if (Test-Path $anStuSketch) {
        Copy-Item $anStuSketch $abmdmsSketch -Force
        Say "   -> Synced poultry_sensor.ino to ABMDMS motion_sensor.ino"
    }

    Say "Synchronization Complete!" -ForegroundColor Green
}
else {
    Say "Status Check Complete. Run with -SyncFromAbmdms or -SyncToAbmdms to sync."
}
Line
