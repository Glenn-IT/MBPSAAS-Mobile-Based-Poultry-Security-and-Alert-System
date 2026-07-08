# MBPSAAS — Project Checklist

Mobile-Based Poultry Security and Alert System.
Android app (Kotlin + Jetpack Compose) + PHP API (XAMPP) + Arduino (later).

> Setting up on a new laptop? Follow `docs/SETUP.md` step by step.

## How to run everything

1. Open **XAMPP Control Panel** → start **Apache** and **MySQL**.
2. PHP API source is the repo's `api/` folder. XAMPP serves it through a
   junction at `C:\xampp\htdocs\mbpsaas_api` (see SETUP.md step 3) — edit the
   files in `api/`, they're live immediately and tracked by git.
3. First time only: open `http://localhost/mbpsaas_api/setup.php` in a browser
   — creates database `mbpsaas_db`, the `users` table, and the test account.
4. Phone and PC must be on the **same Wi-Fi**.
   The app calls `http://192.168.254.104/mbpsaas_api/` — if the PC's IP changes
   (check with `ipconfig`), update `BASE_URL` in `app/src/main/java/.../data/ApiClient.kt`.
   For the Android emulator use `http://10.0.2.2/mbpsaas_api/`.
5. Press Run ▶ in Android Studio.

**The one and only account** (this system uses a single admin — no registration):
username `admin` / password `admin123`
Security question: "What is your favorite animal?" → answer `chicken` (case-insensitive)
The account is created by `setup.php`; to change its details later use the app
(forgot password) or edit the `users` table in phpMyAdmin.

## Done ✅

- [x] Project setup (Compose, Material 3, Retrofit + Gson)
- [x] MySQL database `mbpsaas_db` with `users` table
      (passwords and security answers stored hashed with bcrypt)
- [x] PHP API in the repo's `api/` folder (served by XAMPP via junction):
  - `config.php` — DB connection + JSON helpers
  - `setup.php` — one-time DB/table/test-user creation
  - `login.php` — username (or email) + password
  - `get_questions.php` — list of security questions (for dropdown)
  - `check_user.php` — forgot password step 1 (does the account exist?)
  - `verify_security_answer.php` — step 2: user must pick the CORRECT question
    from a dropdown AND give the correct answer → returns 15-minute reset token
  - `reset_password.php` — step 3: new password with token
- [x] Login screen (with show/hide password)
- [x] Forgot Password screen — 3 steps:
      username → pick question + answer → new password (with show/hide)
- [x] Placeholder Home screen with logout
- [x] Password-reset success dialog
- [x] Tested end-to-end on a real phone over Wi-Fi
- [x] Decision: single admin account only — NO register screen
      (admin is seeded by `setup.php`)

## To do 📋

- [ ] **Stay logged in** — save the user with DataStore/SharedPreferences so the
      app doesn't ask for login every launch
- [ ] **Home dashboard** — real UI replacing the placeholder
- [ ] **Arduino integration** — the poultry security part:
  - [ ] Decide how Arduino talks to the system (Arduino → PHP API endpoint,
        e.g. `sensor_data.php` that inserts readings into MySQL)
  - [ ] Table(s) for sensor readings / events
  - [ ] App screen showing live sensor status
- [ ] **Alerts** — notify the phone when Arduino detects something
      (simplest: app polls an `alerts` table; better: Firebase Cloud Messaging)
- [ ] Alert history screen
- [ ] Settings screen (change password, change security question)
- [ ] Later/nice-to-have: input rate-limiting on login, HTTPS, hosting the API
      somewhere permanent instead of the laptop

## Gotchas we already hit

- Build from terminal needs `JAVA_HOME` = `C:\Program Files\Android\Android Studio1\jbr`
  (Android Studio is installed in the "Android Studio1" folder).
- compileSdk had to be bumped to 37 (androidx.core 1.19.0 requires it).
- `android:usesCleartextTraffic="true"` is set in the manifest because XAMPP
  serves plain HTTP — remove this if the API ever moves to HTTPS.
- If the phone says "Cannot reach server": check same Wi-Fi, Apache running,
  and Windows Firewall allowing Apache (port 80).
