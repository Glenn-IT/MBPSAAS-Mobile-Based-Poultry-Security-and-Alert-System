<?php
// One-time setup: creates the database, users table, and a test account.
// Run in a browser: http://localhost/mbpsaas_api/setup.php
// Safe to run again — it won't duplicate the test user.

header('Content-Type: application/json; charset=utf-8');

mysqli_report(MYSQLI_REPORT_OFF);
$conn = new mysqli('localhost', 'root', '');
if ($conn->connect_error) {
    echo json_encode(['success' => false, 'message' => 'MySQL not running? ' . $conn->connect_error]);
    exit;
}

$conn->query("CREATE DATABASE IF NOT EXISTS mbpsaas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
$conn->select_db('mbpsaas_db');

$conn->query("
    CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        security_question VARCHAR(255) NOT NULL,
        security_answer_hash VARCHAR(255) NOT NULL,
        reset_token VARCHAR(64) DEFAULT NULL,
        reset_token_expires DATETIME DEFAULT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB
");

$conn->query("
    CREATE TABLE IF NOT EXISTS motion_events (
        id INT AUTO_INCREMENT PRIMARY KEY,
        zone VARCHAR(20) NOT NULL DEFAULT 'COOP1',
        detected_at DATETIME NOT NULL,
        buzzer_triggered TINYINT(1) NOT NULL DEFAULT 1,
        note VARCHAR(255) DEFAULT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB
");

// Add 'zone' column to existing motion_events table if it doesn't exist
$colCheck = $conn->query("SHOW COLUMNS FROM motion_events LIKE 'zone'");
if ($colCheck && $colCheck->num_rows === 0) {
    $conn->query("ALTER TABLE motion_events ADD COLUMN zone VARCHAR(20) NOT NULL DEFAULT 'COOP1' AFTER id");
}

$conn->query("
    CREATE TABLE IF NOT EXISTS sensor_zones (
        id INT AUTO_INCREMENT PRIMARY KEY,
        sensor_code VARCHAR(20) NOT NULL UNIQUE,
        name VARCHAR(100) NOT NULL,
        is_enabled TINYINT(1) NOT NULL DEFAULT 1,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB
");

// Seed the 3 PIR sensor zones: COOP1, COOP2, PERIMETER
$initialSensors = [
    ['COOP1', 'Coop 1 - PIR Sensor'],
    ['COOP2', 'Coop 2 - PIR Sensor'],
    ['PERIMETER', 'Perimeter - PIR Sensor'],
];

foreach ($initialSensors as $sensor) {
    $sCode = $sensor[0];
    $sName = $sensor[1];
    $checkSensor = $conn->query("SELECT id FROM sensor_zones WHERE sensor_code = '$sCode'");
    if ($checkSensor && $checkSensor->num_rows === 0) {
        $stmt = $conn->prepare("INSERT INTO sensor_zones (sensor_code, name, is_enabled) VALUES (?, ?, 1)");
        $stmt->bind_param('ss', $sCode, $sName);
        $stmt->execute();
    }
}


// Seed a test account: username "admin", password "admin123",
// security answer "chicken" (answers are stored lowercase + hashed).
$check = $conn->query("SELECT id FROM users WHERE username = 'admin'");
if ($check && $check->num_rows === 0) {
    $stmt = $conn->prepare(
        "INSERT INTO users (username, email, password_hash, security_question, security_answer_hash)
         VALUES (?, ?, ?, ?, ?)"
    );
    $username = 'admin';
    $email = 'admin@mbpsaas.local';
    $passwordHash = password_hash('admin123', PASSWORD_DEFAULT);
    $question = 'What is your favorite animal?';
    $answerHash = password_hash('chicken', PASSWORD_DEFAULT);
    $stmt->bind_param('sssss', $username, $email, $passwordHash, $question, $answerHash);
    $stmt->execute();
    $seeded = true;
} else {
    $seeded = false;
}

echo json_encode([
    'success' => true,
    'message' => 'Database, users table, and motion_events table ready.',
    'test_user_created' => $seeded,
    'test_credentials' => [
        'username' => 'admin',
        'password' => 'admin123',
        'security_answer' => 'chicken',
    ],
]);
