<?php
// POST: toggles the enabled/disabled state of a specific PIR sensor zone.
// Required POST fields: sensor_code, is_enabled (1 or 0)
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$sensorCode = strtoupper(trim(post_field('sensor_code')));
$isEnabledRaw = trim($_POST['is_enabled'] ?? '');

if ($isEnabledRaw === '') {
    respond(false, 'Missing field: is_enabled');
}

$isEnabled = ($isEnabledRaw === '1' || strtolower($isEnabledRaw) === 'true') ? 1 : 0;

$conn = db_connect();

// Check if sensor exists
$checkStmt = $conn->prepare("SELECT id FROM sensor_zones WHERE sensor_code = ?");
$checkStmt->bind_param('s', $sensorCode);
$checkStmt->execute();
$res = $checkStmt->get_result();

if ($res->num_rows === 0) {
    respond(false, "Invalid sensor code: $sensorCode");
}

$stmt = $conn->prepare("UPDATE sensor_zones SET is_enabled = ? WHERE sensor_code = ?");
$stmt->bind_param('is', $isEnabled, $sensorCode);
$stmt->execute();

$statusText = $isEnabled ? 'Enabled' : 'Disabled';
$sensorName = SENSOR_ZONES[$sensorCode] ?? "$sensorCode PIR Sensor";
$logNote = "$sensorName $statusText";

$logStmt = $conn->prepare(
    "INSERT INTO motion_events (zone, detected_at, buzzer_triggered, note) VALUES (?, NOW(), 0, ?)"
);
$logStmt->bind_param('ss', $sensorCode, $logNote);
$logStmt->execute();

respond(true, "Sensor $sensorCode is now " . strtolower($statusText));
