<?php
// POST: called by Arduino Serial Reader / Simulator to record motion events into motion_monitoring DB
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST request required');
}

$eventType = strtoupper(trim($_POST['event_type'] ?? 'MOTION_DETECTED'));
if (!in_array($eventType, ['MOTION_DETECTED', 'MOTION_STOPPED'], true)) {
    $eventType = 'MOTION_DETECTED';
}

$zone = strtoupper(trim($_POST['zone'] ?? 'ROOMC'));
if (!in_array($zone, ALLOWED_ZONES, true)) {
    $zone = 'ROOMC';
}

$source = trim($_POST['source'] ?? 'ARDUINO_PIR');
if ($source === '') {
    $source = 'ARDUINO_PIR';
}

$conn = db_connect();

// Check if motion_logs table exists, else fallback to motion_events
$tableCheck = $conn->query("SHOW TABLES LIKE 'motion_logs'");
$tableName = ($tableCheck && $tableCheck->num_rows > 0) ? 'motion_logs' : 'motion_events';

if ($tableName === 'motion_logs') {
    $stmt = $conn->prepare(
        "INSERT INTO motion_logs (event_type, zone, source, detected_at) VALUES (?, ?, ?, NOW())"
    );
    $stmt->bind_param('sss', $eventType, $zone, $source);
} else {
    $buzzerTriggered = ($eventType === 'MOTION_DETECTED') ? 1 : 0;
    $note = "Event: $eventType from $source";
    $stmt = $conn->prepare(
        "INSERT INTO motion_events (zone, detected_at, buzzer_triggered, note) VALUES (?, NOW(), ?, ?)"
    );
    $stmt->bind_param('sis', $zone, $buzzerTriggered, $note);
}

if ($stmt->execute()) {
    respond(true, 'Motion event recorded successfully', [
        'id' => $stmt->insert_id,
        'event_type' => $eventType,
        'zone' => $zone,
        'source' => $source
    ]);
} else {
    respond(false, 'Failed to insert motion event into database');
}
