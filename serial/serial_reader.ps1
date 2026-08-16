# ============================================================
#  MBPSAAS - PowerShell Serial Bridge Listener (Auto-Detect)
#  File: serial/serial_reader.ps1
# ============================================================

param(
    [string]$ComPort = "AUTO",
    [int]$BaudRate = 9600
)

$ApiUrl = "http://localhost/mbpsaas_api/log_motion_event.php"
$Source = "ARDUINO_PIR"
$DuplicateWindow = 2

function Say([string]$text) {
    Write-Host ("[" + (Get-Date -Format "HH:mm:ss") + "] " + $text)
}

function Line() { Write-Host ("-" * 58) }

function Resolve-AutoComPort([string]$requestedPort) {
    if ($requestedPort -and $requestedPort -ne "AUTO" -and $requestedPort -ne "") {
        return $requestedPort
    }

    # Auto-detect available COM ports on Windows
    try {
        $availablePorts = [System.IO.Ports.SerialPort]::GetPortNames()
        if ($availablePorts -and $availablePorts.Count -gt 0) {
            $detected = $availablePorts | Select-Object -First 1
            Say ("AUTO-DETECTED Serial Port: " + $detected)
            return $detected
        }
    }
    catch {
        # Fallback if GetPortNames fails
    }

    return "COM5" # Default fallback
}

$ZonePattern = '^(ROOMA|ROOMB|ROOMC|ROOMD|COOP1|COOP2|COOP3|PERIMETER)_(MOTION_DETECTED|MOTION_STOPPED)$'

$lastEvent = ""
$lastTime  = Get-Date "2000-01-01"

while ($true) {
    $targetPort = Resolve-AutoComPort $ComPort

    Line
    Write-Host "  MBPSAAS - Arduino Serial Reader (Auto-Resolving Bridge)"
    Line
    Write-Host ("  COM Port : " + $targetPort + " (Requested: " + $ComPort + ")")
    Write-Host ("  Baud Rate: " + $BaudRate)
    Write-Host ("  API URL  : " + $ApiUrl)
    Line
    Write-Host "  Press Ctrl + C to stop."
    Write-Host "  !! Make sure the Arduino IDE Serial Monitor is CLOSED !!"
    Line
    Write-Host ""

    Say ("Opening serial port " + $targetPort + " at " + $BaudRate + " baud...")

    $port = New-Object System.IO.Ports.SerialPort $targetPort, $BaudRate, None, 8, One
    $port.ReadTimeout = 1000

    try {
        $port.Open()
    }
    catch {
        Say ("ERROR: Could not open " + $targetPort + ".")
        Say ("Reason: " + $_.Exception.Message)
        
        try {
            $allPorts = [System.IO.Ports.SerialPort]::GetPortNames()
            if ($allPorts -and $allPorts.Count -gt 0) {
                Say ("Available COM ports on this system: " + ($allPorts -join ", "))
            } else {
                Say ("No active COM ports detected. Check USB connection to Arduino.")
            }
        }
        catch {}

        Say "Retrying in 3 seconds... (Ensure Arduino IDE Serial Monitor is closed)"
        Start-Sleep -Seconds 3
        continue
    }

    Say ("CONNECTED to " + $targetPort + "! Listening for motion events...")
    Line

    while ($port.IsOpen) {
        try {
            $line = $port.ReadLine().Trim()
        }
        catch [TimeoutException] {
            continue
        }
        catch {
            Say ("Connection lost: " + $_.Exception.Message)
            break
        }

        if ([string]::IsNullOrWhiteSpace($line)) { continue }

        Say ("SERIAL IN > " + $line)

        if ($line -match $ZonePattern) {
            $zone = $Matches[1].ToUpper()
            $eventType = $Matches[2].ToUpper()
            $eventKey = $zone + "_" + $eventType
            $now = Get-Date

            if ($eventKey -eq $lastEvent -and ($now - $lastTime).TotalSeconds -lt $DuplicateWindow) {
                Say "   -> Duplicate event ignored (cooldown)"
                continue
            }

            $lastEvent = $eventKey
            $lastTime  = $now

            Say ("   -> Forwarding " + $eventType + " for " + $zone + " to API...")

            try {
                $body = @{
                    event_type = $eventType
                    zone       = $zone
                    source     = $Source
                }
                $response = Invoke-RestMethod -Uri $ApiUrl -Method Post -Body $body -TimeoutSec 5
                if ($response.success) {
                    Say ("   -> API SUCCESS: Saved event ID #" + $response.id)
                } else {
                    Say ("   -> API ERROR: " + $response.message)
                }
            }
            catch {
                Say ("   -> API HTTP ERROR: " + $_.Exception.Message)
            }
        }
    }

    if ($port.IsOpen) { $port.Close() }
    $port.Dispose()
    Say "Reconnecting in 2 seconds..."
    Start-Sleep -Seconds 2
}
