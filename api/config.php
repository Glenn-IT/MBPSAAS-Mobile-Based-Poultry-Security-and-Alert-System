<?php
// MBPSAAS API - Database configuration
// Using ABMDMS database 'motion_monitoring' directly
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'motion_monitoring');

header('Content-Type: application/json; charset=utf-8');

// The fixed list of security questions shown in the forgot-password dropdown.
const SECURITY_QUESTIONS = [
    'What is your favorite animal?',
    'What is the name of your first pet?',
    'What is your mother\'s maiden name?',
    'In what city were you born?',
    'What was the name of your elementary school?',
    'What is your favorite food?',
];

const ALLOWED_ZONES = ['ROOMA', 'ROOMB', 'ROOMC', 'ROOMD', 'COOP1', 'COOP2', 'COOP3', 'PERIMETER'];

const ZONE_LABELS = [
    'ROOMA' => 'Coop Zone A',
    'ROOMB' => 'Coop Zone B',
    'ROOMC' => 'Coop Zone C',
    'ROOMD' => 'Perimeter Gate',
    'COOP1' => 'Coop Zone A',
    'COOP2' => 'Coop Zone B',
    'COOP3' => 'Coop Zone C',
    'PERIMETER' => 'Perimeter Gate',
];

function db_connect(): mysqli
{
    mysqli_report(MYSQLI_REPORT_OFF);
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    if ($conn->connect_error) {
        respond(false, 'Database connection failed. Is MySQL running in XAMPP?');
    }
    $conn->set_charset('utf8mb4');
    return $conn;
}

// Send a JSON response and stop. Extra fields are merged into the payload.
function respond(bool $success, string $message, array $extra = []): void
{
    echo json_encode(array_merge([
        'success' => $success,
        'message' => $message,
    ], $extra));
    exit;
}

// Read a required POST field or fail with a clear message.
function post_field(string $name): string
{
    $value = trim($_POST[$name] ?? '');
    if ($value === '') {
        respond(false, "Missing field: $name");
    }
    return $value;
}
