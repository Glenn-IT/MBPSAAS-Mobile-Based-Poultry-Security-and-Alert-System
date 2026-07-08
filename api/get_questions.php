<?php
// Returns the full list of security questions (for dropdowns).
// Deliberately NOT user-specific — the app must never reveal which
// question a particular user actually chose.
require_once __DIR__ . '/config.php';

respond(true, 'Questions list', ['questions' => SECURITY_QUESTIONS]);
