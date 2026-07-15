<?php
// POST: called by the Arduino/ESP when the motion sensor fires and the buzzer sounds.
// Optional fields: buzzer_triggered ("1"/"0", default "1"), note (free text).
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$buzzerTriggered = ($_POST['buzzer_triggered'] ?? '1') === '0' ? 0 : 1;
$note = trim($_POST['note'] ?? '');

$conn = db_connect();
$stmt = $conn->prepare(
    "INSERT INTO motion_events (detected_at, buzzer_triggered, note) VALUES (NOW(), ?, ?)"
);
$stmt->bind_param('is', $buzzerTriggered, $note);
$stmt->execute();

respond(true, 'Motion event logged', ['id' => $stmt->insert_id]);
