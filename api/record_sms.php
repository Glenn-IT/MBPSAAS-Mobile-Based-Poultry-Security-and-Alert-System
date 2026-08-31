<?php
// POST: called by Arduino Serial Reader to record SMS alerts into motion_monitoring DB (sms_logs table)
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST request required');
}

$zone = strtoupper(trim($_POST['zone'] ?? ''));
if (!in_array($zone, ALLOWED_ZONES, true)) {
    respond(false, 'Invalid or missing zone');
}

$status = strtoupper(trim($_POST['status'] ?? 'SENT'));
$allowedStatuses = ['SENT', 'FAILED', 'SKIPPED'];
if (!in_array($status, $allowedStatuses, true)) {
    $status = 'FAILED';
}

$detail = strtoupper(substr(preg_replace('/[^A-Za-z0-9_\- ]/', '', trim($_POST['detail'] ?? '')), 0, 100));
$recipient = substr(preg_replace('/[^0-9+]/', '', trim($_POST['recipient'] ?? '+639169751409')), 0, 20);

$conn = db_connect();

// Ensure sms_logs table exists
$tableCheck = $conn->query("SHOW TABLES LIKE 'sms_logs'");
if (!$tableCheck || $tableCheck->num_rows === 0) {
    // Create sms_logs table if it doesn't exist
    $conn->query("CREATE TABLE IF NOT EXISTS sms_logs (
        id INT AUTO_INCREMENT PRIMARY KEY,
        zone VARCHAR(20) NOT NULL,
        recipient VARCHAR(20) NOT NULL,
        status ENUM('SENT', 'FAILED', 'SKIPPED') NOT NULL,
        detail VARCHAR(100) NULL,
        sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_zone (zone),
        INDEX idx_sent_at (sent_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
}

$stmt = $conn->prepare(
    "INSERT INTO sms_logs (zone, recipient, status, detail, sent_at) VALUES (?, ?, ?, ?, NOW())"
);
$stmt->bind_param('ssss', $zone, $recipient, $status, $detail);

if ($stmt->execute()) {
    respond(true, 'SMS alert recorded successfully', [
        'id' => $stmt->insert_id,
        'zone' => $zone,
        'recipient' => $recipient,
        'status' => $status,
        'detail' => $detail
    ]);
} else {
    respond(false, 'Failed to insert SMS alert into database');
}
