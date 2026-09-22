<?php
// GET: return motion events from motion_monitoring database for MBPSAAS Android App
require_once __DIR__ . '/config.php';

$conn = db_connect();

// Check if motion_logs table exists, else fallback to motion_events
$tableCheck = $conn->query("SHOW TABLES LIKE 'motion_logs'");
$tableName = ($tableCheck && $tableCheck->num_rows > 0) ? 'motion_logs' : 'motion_events';

if ($tableName === 'motion_logs') {
    $result = $conn->query(
        "SELECT id, event_type, zone, source, detected_at, created_at FROM motion_logs ORDER BY detected_at DESC, id DESC LIMIT 50"
    );
} else {
    $result = $conn->query(
        "SELECT id, 'MOTION_DETECTED' as event_type, zone, 'ARDUINO_PIR' as source, detected_at, created_at FROM motion_events ORDER BY detected_at DESC, id DESC LIMIT 50"
    );
}

$events = [];
$latestEvent = null;
$overallStatus = 'NO_MOTION';
$zoneMap = [
    'ROOMA' => ['label' => 'Coop Zone A', 'status' => 'NO_MOTION', 'last_motion' => null],
    'ROOMB' => ['label' => 'Coop Zone B', 'status' => 'NO_MOTION', 'last_motion' => null],
    'ROOMC' => ['label' => 'Coop Zone C', 'status' => 'NO_MOTION', 'last_motion' => null],
];

// Check which sensor zones are disabled by the user
$disabledZones = [];
$sensorZonesCheck = $conn->query("SHOW TABLES LIKE 'sensor_zones'");
if ($sensorZonesCheck && $sensorZonesCheck->num_rows > 0) {
    $szResult = $conn->query("SELECT sensor_code, is_enabled FROM sensor_zones");
    if ($szResult) {
        while ($szRow = $szResult->fetch_assoc()) {
            if (!(bool)$szRow['is_enabled']) {
                $disabledZones[strtoupper($szRow['sensor_code'])] = true;
            }
        }
    }
}

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $eventType = strtoupper($row['event_type'] ?? 'MOTION_DETECTED');
        $zoneCode  = strtoupper($row['zone'] ?? 'ROOMC');
        $zoneLabel = ZONE_LABELS[$zoneCode] ?? $zoneCode;
        $isMotion  = ($eventType === 'MOTION_DETECTED');

        $eventItem = [
            'id' => (int) $row['id'],
            'event_type' => $eventType,
            'zone' => $zoneCode,
            'zone_label' => $zoneLabel,
            'source' => $row['source'] ?? 'ARDUINO_PIR',
            'detected_at' => $row['detected_at'],
            'buzzer_triggered' => $isMotion,
            'note' => null,
        ];

        $events[] = $eventItem;

        if ($latestEvent === null) {
            $latestEvent = $eventItem;
            // Only trigger overall active motion status if this zone is currently enabled
            if ($isMotion && !isset($disabledZones[$zoneCode])) {
                $overallStatus = 'MOTION_DETECTED';
            }
        }

        // Update zone status map for 3 coop zones
        $mappedZoneKey = match ($zoneCode) {
            'COOP1' => 'ROOMA',
            'COOP2' => 'ROOMB',
            'COOP3' => 'ROOMC',
            default => $zoneCode,
        };

        if (array_key_exists($mappedZoneKey, $zoneMap) && $zoneMap[$mappedZoneKey]['last_motion'] === null) {
            if (isset($disabledZones[$mappedZoneKey])) {
                $zoneMap[$mappedZoneKey]['status'] = 'DISABLED';
            } else {
                $zoneMap[$mappedZoneKey]['status'] = $eventType;
            }
            $zoneMap[$mappedZoneKey]['last_motion'] = $row['detected_at'];
        }
    }
}

// Mark any remaining disabled zones in the zoneMap
foreach ($zoneMap as $zk => $zv) {
    if (isset($disabledZones[$zk])) {
        $zoneMap[$zk]['status'] = 'DISABLED';
    }
}

// Counts
$totalCountResult = $conn->query("SELECT COUNT(*) as total FROM $tableName");
$totalEvents = $totalCountResult ? (int) ($totalCountResult->fetch_assoc()['total'] ?? 0) : count($events);

$todayCountResult = $conn->query("SELECT COUNT(*) as today FROM $tableName WHERE DATE(detected_at) = CURDATE()");
$todayEvents = $todayCountResult ? (int) ($todayCountResult->fetch_assoc()['today'] ?? 0) : 0;

respond(true, 'Motion events retrieved', [
    'status' => $overallStatus,
    'total_events' => $totalEvents,
    'today_events' => $todayEvents,
    'last_motion' => $latestEvent['detected_at'] ?? null,
    'zones' => $zoneMap,
    'events' => $events,
]);
