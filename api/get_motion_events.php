<?php
// GET: most recent motion/buzzer events, newest first, for the app's alert log.
require_once __DIR__ . '/config.php';

$conn = db_connect();
$result = $conn->query(
    "SELECT id, zone, detected_at, buzzer_triggered, note FROM motion_events ORDER BY detected_at DESC LIMIT 50"
);

$events = [];
while ($row = $result->fetch_assoc()) {
    $events[] = [
        'id' => (int) $row['id'],
        'zone' => $row['zone'] ?? 'COOP1',
        'detected_at' => $row['detected_at'],
        'buzzer_triggered' => (bool) $row['buzzer_triggered'],
        'note' => $row['note'],
    ];
}

respond(true, 'Motion events', ['events' => $events]);
