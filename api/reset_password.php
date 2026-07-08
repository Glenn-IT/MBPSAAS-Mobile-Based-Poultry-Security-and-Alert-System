<?php
// Forgot password - step 3
// POST: username, reset_token, new_password
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$username = post_field('username');
$token = post_field('reset_token');
$newPassword = post_field('new_password');

if (strlen($newPassword) < 6) {
    respond(false, 'Password must be at least 6 characters');
}

$conn = db_connect();
$stmt = $conn->prepare(
    "SELECT id, reset_token FROM users
     WHERE (username = ? OR email = ?) AND reset_token_expires > NOW()"
);
$stmt->bind_param('ss', $username, $username);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();

if (!$user || !hash_equals($user['reset_token'] ?? '', $token)) {
    respond(false, 'Invalid or expired reset token. Please start over.');
}

$passwordHash = password_hash($newPassword, PASSWORD_DEFAULT);
$stmt = $conn->prepare(
    "UPDATE users SET password_hash = ?, reset_token = NULL, reset_token_expires = NULL WHERE id = ?"
);
$stmt->bind_param('si', $passwordHash, $user['id']);
$stmt->execute();

respond(true, 'Password reset successful. You can now log in.');
