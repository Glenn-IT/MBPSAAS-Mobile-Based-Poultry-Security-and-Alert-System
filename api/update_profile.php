<?php
// Update the admin's own username/email/password.
// POST: user_id, current_password, new_username, new_email, new_password (optional)
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$userId = (int) post_field('user_id');
$currentPassword = post_field('current_password');
$newUsername = post_field('new_username');
$newEmail = post_field('new_email');
$newPassword = trim($_POST['new_password'] ?? '');

if ($newPassword !== '' && strlen($newPassword) < 6) {
    respond(false, 'New password must be at least 6 characters');
}

$conn = db_connect();
$stmt = $conn->prepare("SELECT id, password_hash FROM users WHERE id = ?");
$stmt->bind_param('i', $userId);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();

if (!$user || !password_verify($currentPassword, $user['password_hash'])) {
    respond(false, 'Current password is incorrect');
}

$stmt = $conn->prepare("SELECT id FROM users WHERE (username = ? OR email = ?) AND id != ?");
$stmt->bind_param('ssi', $newUsername, $newEmail, $userId);
$stmt->execute();
if ($stmt->get_result()->fetch_assoc()) {
    respond(false, 'Username or email already in use');
}

if ($newPassword !== '') {
    $passwordHash = password_hash($newPassword, PASSWORD_DEFAULT);
    $stmt = $conn->prepare("UPDATE users SET username = ?, email = ?, password_hash = ? WHERE id = ?");
    $stmt->bind_param('sssi', $newUsername, $newEmail, $passwordHash, $userId);
} else {
    $stmt = $conn->prepare("UPDATE users SET username = ?, email = ? WHERE id = ?");
    $stmt->bind_param('ssi', $newUsername, $newEmail, $userId);
}
$stmt->execute();

respond(true, 'Profile updated successfully', [
    'user' => [
        'id' => $userId,
        'username' => $newUsername,
        'email' => $newEmail,
    ],
]);
