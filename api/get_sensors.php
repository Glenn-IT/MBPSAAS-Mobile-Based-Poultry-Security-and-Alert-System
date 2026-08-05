<?php
// GET: returns all registered PIR sensor zones and their enabled/disabled status.
require_once __DIR__ . '/config.php';

$conn = db_connect();
$result = $conn->query("SELECT id, sensor_code, name, is_enabled FROM sensor_zones ORDER BY id ASC");

$sensors = [];
if ($result) {
    while ($row = $result->fetch_assoc()) {
        $sensors[] = [
            'id' => (int) $row['id'],
            'sensor_code' => $row['sensor_code'],
            'name' => $row['name'],
            'is_enabled' => (bool) $row['is_enabled'],
        ];
    }
}

respond(true, 'Sensors retrieved successfully', ['sensors' => $sensors]);
