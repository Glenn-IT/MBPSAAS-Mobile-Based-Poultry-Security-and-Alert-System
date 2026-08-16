# MBPSAAS — Arduino & Android Integration Checklist (Using ABMDMS Database)

Use this checklist to complete and verify the integration of the **Arduino hardware (COM5)**, **ABMDMS MySQL database (`motion_monitoring`)**, **PHP REST API**, and the **MBPSAAS Android Application**.

---

## Phase 1 — Database Verification (`motion_monitoring`) — [COMPLETED ✅]

- [x] Open **XAMPP Control Panel** and start **Apache** and **MySQL**.
- [x] Open phpMyAdmin at `http://localhost/phpmyadmin`.
- [x] Confirm database **`motion_monitoring`** exists:
  - [x] Confirm table **`motion_logs`** exists with columns: `id`, `event_type`, `zone`, `source`, `detected_at`, `created_at`.
  - [x] Confirm table **`sms_logs`** exists with columns: `id`, `zone`, `recipient`, `status`, `detail`, `sent_at`, `created_at`.

---

## Phase 2 — PHP API Configuration (`api/`) — [COMPLETED ✅]

- [x] Confirm XAMPP junction or copy exists at `C:\xampp\htdocs\mbpsaas_api`.
- [x] Update `api/config.php` to target the ABMDMS database (`motion_monitoring`).
- [x] Update `api/log_motion_event.php` to insert `event_type`, `zone`, `source` into `motion_logs`.
- [x] Update `api/get_motion_events.php` to fetch motion status, zone breakdown, and event history from `motion_logs`.

---

## Phase 3 — Web Simulator Verification (No Hardware Required) — [COMPLETED ✅]

- [x] Create/open `tools/simulate_motion.php` at `http://localhost/mbpsaas_api/tools/simulate_motion.php`.
- [x] Click **Check API Connection** and confirm it returns `API OK`.
- [x] Click **Simulate MOTION_DETECTED** for `ROOMA` (Coop Zone A).
- [x] Open phpMyAdmin -> `motion_monitoring.motion_logs` and confirm a new row was inserted.
- [x] Click **Simulate MOTION_STOPPED** for `ROOMA`.
- [x] Open `http://localhost/mbpsaas_api/get_motion_events.php` in your browser and confirm JSON returns multi-zone status.

---

## Phase 4 — Android Application Integration (`app/`) — [COMPLETED ✅]

- [x] **Configure Base URL**: `ApiClient.kt` configured for USB tunneling (`http://localhost:8080/mbpsaas_api/`) & LAN IP.
- [x] **Data Models (`data/ApiModels.kt`)**: Added `ZoneStatus`, extended `MotionEvent` and `MotionEventsResponse`.
- [x] **API Service Interface (`data/ApiService.kt`)**: Mapped `@GET("get_motion_events.php")`.
- [x] **UI Screens (`ui/DashboardScreen.kt`, `MotionEventComponents.kt`, `HomeScreen.kt`)**: Real-time status cards, `ZoneStatusGrid`, and 3-second live polling loop.
- [x] **Build Verification**: Gradle build verified (`./gradlew compileDebugKotlin` -> BUILD SUCCESSFUL).

---

## Phase 5 — Hardware & Arduino Firmware Setup (COM5) — [COMPLETED ✅]

- [x] **Circuit Assembly**:
  - PIR Sensor 1 OUT -> Arduino Digital **Pin 2** (`ROOMC` / Coop Zone C)
  - PIR Sensor 2 OUT -> Arduino Digital **Pin 3** (`ROOMA` / Coop Zone A)
  - PIR Sensor 3 OUT -> Arduino Digital **Pin 4** (`ROOMB` / Coop Zone B)
  - Farm Alarm Buzzer (+) -> Arduino Digital **Pin 8**
  - SIM800L Module TXD -> Pin 10, RXD -> Pin 11, RST -> Pin 12 (Powered via External 5V/2A + 1000µF capacitor)
- [x] **Upload Arduino Sketch**:
  - Open Arduino IDE -> `arduino/poultry_sensor/poultry_sensor.ino`.
  - Select Board: **Arduino Uno**, Port: **COM5**.
  - Upload sketch and verify `System Ready` output.

---

## Phase 6 — Serial Reader Bridge Execution (COM5) — [COMPLETED ✅]

- [x] Open PowerShell in project directory: `cd C:\Users\GLENN\AndroidStudioProjects\MBPSAASMobileBasedPoultrySecurityandAlertSystem\serial`
- [x] Run serial bridge for COM5:
  ```cmd
  .\start_reader.bat COM5
  ```
- [x] Confirm terminal logs `[READY] Listening for serial events on COM5...`

---

## Phase 7 — End-to-End System Testing & Sign-Off — [IN PROGRESS 🚀]

- [ ] **Step 7.1 — Physical Motion Trigger**:
  - Wave hand in front of Coop Zone A PIR Sensor (Pin 3).
  - [ ] Local alarm buzzer sounds on Pin 8.
- [ ] **Step 7.2 — Serial Bridge & API Log Verification**:
  - [ ] Terminal window logs `[SERIAL IN] ROOMA_MOTION_DETECTED`.
  - [ ] Terminal window logs `[API POST] Forwarding MOTION_DETECTED... HTTP 200 OK`.
- [ ] **Step 7.3 — GSM SMS Alert Verification**:
  - [ ] SIM800L module sends SMS alert `[MBPSAAS ALERT] Intrusion detected in Coop Zone A` to recipient mobile phone.
- [ ] **Step 7.4 — Android App Real-Time Update**:
  - [ ] Android App screen updates to **INTRUSION ALERT — Coop Zone A** (Red) within 3 seconds.
  - [ ] Zone card for `Coop Zone A` highlights **MOTION**.
- [ ] **Step 7.5 — Motion Clearance**:
  - Hold still -> Confirm PIR sensor returns to clear state.
  - [ ] Terminal window logs `[SERIAL IN] ROOMA_MOTION_STOPPED`.
  - [ ] Local alarm buzzer turns OFF.
  - [ ] Android App returns to **ALL POULTRY ZONES SAFE** (Green).

---

## Final Milestone Sign-Off Table

| Milestone | Date | Verified By | Status |
|---|---|---|---|
| Database & API Configured | 2026-08-16 | Software Lead | [x] Completed |
| Web Simulator Validated | 2026-08-16 | Software Lead | [x] Completed |
| Android App Models & UI (Phase 4) | 2026-08-16 | Android Dev | [x] Completed |
| Arduino Firmware (COM5) Uploaded | 2026-08-16 | Hardware Lead | [x] Completed |
| Serial Bridge Running (COM5) | 2026-08-16 | Systems Admin | [x] Completed |
| **End-to-End System Sign-Off** | **2026-08-16** | **Project Manager** | **[ ] Testing Now** |
