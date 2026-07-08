<?php
// Forgot password - step 1
// POST: username (or email) -> confirms the account exists.
// Does NOT return the security question; the user must pick it themselves.
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$username = post_field('username');

$conn = db_connect();
$stmt = $conn->prepare("SELECT id FROM users WHERE username = ? OR email = ?");
$stmt->bind_param('ss', $username, $username);
$stmt->execute();

if (!$stmt->get_result()->fetch_assoc()) {
    respond(false, 'No account found with that username or email');
}

respond(true, 'Account found');
