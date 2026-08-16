<?php
// POST: username (or email), password
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$username = post_field('username');
$password = post_field('password');

$conn = db_connect();
$stmt = $conn->prepare(
    "SELECT id, username, email, password_hash FROM users WHERE username = ? OR email = ?"
);

if (!$stmt) {
    respond(false, 'Database table setup error: ' . $conn->error);
}

$stmt->bind_param('ss', $username, $username);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();

if (!$user || !password_verify($password, $user['password_hash'])) {
    respond(false, 'Invalid username or password');
}

respond(true, 'Login successful', [
    'user' => [
        'id' => (int) $user['id'],
        'username' => $user['username'],
        'email' => $user['email'],
    ],
]);
