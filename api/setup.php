<?php
// MBPSAAS Database Setup for motion_monitoring database
header('Content-Type: application/json; charset=utf-8');

mysqli_report(MYSQLI_REPORT_OFF);
$conn = new mysqli('localhost', 'root', '');
if ($conn->connect_error) {
    echo json_encode(['success' => false, 'message' => 'MySQL connection error: ' . $conn->connect_error]);
    exit;
}

// 1. Create motion_monitoring database if not exists
$conn->query("CREATE DATABASE IF NOT EXISTS motion_monitoring CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
$conn->select_db('motion_monitoring');

// 2. Create users table
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
");

// 3. Create motion_logs table
$conn->query("
    CREATE TABLE IF NOT EXISTS motion_logs (
        id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        event_type VARCHAR(20) NOT NULL,
        zone VARCHAR(20) NOT NULL DEFAULT 'ROOMC',
        source VARCHAR(50) NOT NULL DEFAULT 'ARDUINO_PIR',
        detected_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        KEY idx_detected_at (detected_at),
        KEY idx_event_type (event_type),
        KEY idx_zone (zone)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
");

// 4. Create sms_logs table
$conn->query("
    CREATE TABLE IF NOT EXISTS sms_logs (
        id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        zone VARCHAR(20) NOT NULL,
        recipient VARCHAR(20) NOT NULL DEFAULT '',
        status VARCHAR(20) NOT NULL,
        detail VARCHAR(100) NOT NULL DEFAULT '',
        sent_at DATETIME NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
");

// 5. Create sensor_zones table
$conn->query("
    CREATE TABLE IF NOT EXISTS sensor_zones (
        id INT AUTO_INCREMENT PRIMARY KEY,
        sensor_code VARCHAR(20) NOT NULL UNIQUE,
        name VARCHAR(100) NOT NULL,
        is_enabled TINYINT(1) NOT NULL DEFAULT 1,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
");

// Remove ROOMD/PERIMETER if it exists
$conn->query("DELETE FROM sensor_zones WHERE sensor_code IN ('ROOMD', 'PERIMETER')");

// Seed 3 PIR sensor zones: ROOMA, ROOMB, ROOMC
$initialSensors = [
    ['ROOMA', 'Coop Zone A'],
    ['ROOMB', 'Coop Zone B'],
    ['ROOMC', 'Coop Zone C'],
];

foreach ($initialSensors as $sensor) {
    $sCode = $sensor[0];
    $sName = $sensor[1];
    $checkSensor = $conn->query("SELECT id FROM sensor_zones WHERE sensor_code = '$sCode'");
    if ($checkSensor && $checkSensor->num_rows === 0) {
        $stmt = $conn->prepare("INSERT INTO sensor_zones (sensor_code, name, is_enabled) VALUES (?, ?, 1)");
        if ($stmt) {
            $stmt->bind_param('ss', $sCode, $sName);
            $stmt->execute();
        }
    }
}

// Seed default admin account (username: admin, password: admin123)
$check = $conn->query("SELECT id FROM users WHERE username = 'admin'");
if ($check && $check->num_rows === 0) {
    $stmt = $conn->prepare(
        "INSERT INTO users (username, email, password_hash, security_question, security_answer_hash)
         VALUES (?, ?, ?, ?, ?)"
    );
    if ($stmt) {
        $username = 'admin';
        $email = 'admin@mbpsaas.local';
        $passwordHash = password_hash('admin123', PASSWORD_DEFAULT);
        $question = 'What is your favorite animal?';
        $answerHash = password_hash('chicken', PASSWORD_DEFAULT);
        $stmt->bind_param('sssss', $username, $email, $passwordHash, $question, $answerHash);
        $stmt->execute();
    }
}

echo json_encode([
    'success' => true,
    'message' => 'Database motion_monitoring ready with 3 Coop Zones (ROOMA, ROOMB, ROOMC).',
    'test_credentials' => [
        'username' => 'admin',
        'password' => 'admin123',
        'security_answer' => 'chicken'
    ]
]);
