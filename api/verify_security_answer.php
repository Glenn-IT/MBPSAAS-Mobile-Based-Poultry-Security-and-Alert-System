<?php
// Forgot password - step 2
// POST: username, security_question, security_answer -> returns a short-lived reset_token
// The user must pick THEIR question from the dropdown AND answer it correctly.
require_once __DIR__ . '/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(false, 'POST required');
}

$username = post_field('username');
$question = post_field('security_question');
$answer = strtolower(post_field('security_answer'));

$conn = db_connect();
$stmt = $conn->prepare(
    "SELECT id, security_question, security_answer_hash FROM users WHERE username = ? OR email = ?"
);
$stmt->bind_param('ss', $username, $username);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();

// One combined check so we never reveal whether the question or the answer was wrong.
if (
    !$user
    || $question !== $user['security_question']
    || !password_verify($answer, $user['security_answer_hash'])
) {
    respond(false, 'Incorrect security question or answer');
}

// Answer is correct: issue a token valid for 15 minutes.
$token = bin2hex(random_bytes(32));
$stmt = $conn->prepare(
    "UPDATE users SET reset_token = ?, reset_token_expires = DATE_ADD(NOW(), INTERVAL 15 MINUTE) WHERE id = ?"
);
$stmt->bind_param('si', $token, $user['id']);
$stmt->execute();

respond(true, 'Answer verified', ['reset_token' => $token]);
