<?php
/**
 * MBPSAAS - PHP Serial Reader Bridge (COM5)
 */
date_default_timezone_set('Asia/Manila');

$portName = $argv[1] ?? 'COM5';

// Windows port format: COM5 -> COM5: (colon required for ports < 10)
// COM12 -> \\.\COM12
if (preg_match('/^COM(\d+)$/i', $portName, $matches)) {
    $portNum = (int)$matches[1];
    $devicePath = ($portNum >= 10) ? "\\\\.\\COM{$portNum}" : "COM{$portNum}:";
} else {
    $devicePath = $portName;
}

$apiUrl = 'http://localhost/mbpsaas_api/log_motion_event.php';

echo "============================================================\n";
echo " MBPSAAS Serial Reader Bridge (PHP)\n";
echo " Target Port : {$portName} ({$devicePath})\n";
echo " API Endpoint: {$apiUrl}\n";
echo " Time        : " . date('Y-m-d H:i:s') . "\n";
echo "============================================================\n\n";

echo "[SYSTEM] Configuring serial port {$portName} (9600 baud, 8N1)...\n";
exec("mode {$portName} BAUD=9600 PARITY=n DATA=8 STOP=1 xon=off odsr=off octs=off dtr=on rts=on");

$lastEventTime = [];
$cooldownSeconds = 2;

while (true) {
    echo "[CONNECTING] Opening serial stream on {$devicePath}...\n";
    $handle = @fopen($devicePath, "r+b");

    if (!$handle) {
        echo "[ERROR] Failed to open {$portName}. Retrying in 3 seconds...\n";
        echo "        -> Make sure the Arduino IDE Serial Monitor is CLOSED!\n";
        sleep(3);
        continue;
    }

    echo "[READY] Listening for serial events on {$portName}...\n";

    while (!feof($handle)) {
        $line = fgets($handle);
        if ($line === false) {
            usleep(100000);
            continue;
        }

        $line = trim($line);
        if (empty($line)) continue;

        echo "[SERIAL IN] " . date('H:i:s') . " > " . $line . "\n";

        if (preg_match('/^(ROOMA|ROOMB|ROOMC|ROOMD|COOP1|COOP2|COOP3|PERIMETER)_(MOTION_DETECTED|MOTION_STOPPED)$/i', $line, $matches)) {
            $zone = strtoupper($matches[1]);
            $eventType = strtoupper($matches[2]);
            $now = time();

            $eventKey = "{$zone}_{$eventType}";
            if (isset($lastEventTime[$eventKey]) && ($now - $lastEventTime[$eventKey]) < $cooldownSeconds) {
                echo "  -> [SKIP] Duplicate event ignored (cooldown)\n";
                continue;
            }
            $lastEventTime[$eventKey] = $now;

            echo "  -> [API POST] Forwarding {$eventType} for zone {$zone}...\n";
            postToApi($apiUrl, $eventType, $zone, 'ARDUINO_PIR');
        }
    }

    fclose($handle);
    echo "[WARNING] Serial connection lost. Reconnecting in 2 seconds...\n";
    sleep(2);
}

function postToApi(string $url, string $eventType, string $zone, string $source): void {
    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => http_build_query([
            'event_type' => $eventType,
            'zone' => $zone,
            'source' => $source
        ]),
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 5,
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode === 200) {
        echo "  -> [API OK] " . $response . "\n";
    } else {
        echo "  -> [API ERROR] HTTP {$httpCode} | Response: {$response}\n";
    }
}
