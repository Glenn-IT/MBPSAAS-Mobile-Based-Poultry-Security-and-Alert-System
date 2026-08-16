# MBPSAAS — Project Checklist

Mobile-Based Poultry Security and Alert System.
Android app (Kotlin + Jetpack Compose) + PHP API (XAMPP) + Arduino (COM5).

> Complete System Documentation: [`docs/COMPLETE_INTEGRATION_SUMMARY.md`](docs/COMPLETE_INTEGRATION_SUMMARY.md)  
> Integration Plan & Roadmap: [`docs/ARDUINO_INTEGRATION_PLAN.md`](docs/ARDUINO_INTEGRATION_PLAN.md)  
> Step-by-Step Checklist: [`docs/ARDUINO_INTEGRATION_CHECKLIST.md`](docs/ARDUINO_INTEGRATION_CHECKLIST.md)

---

## How to run everything

1. Open **XAMPP Control Panel** → start **Apache** and **MySQL**.
2. Run database setup (if first time): `http://localhost/mbpsaas_api/setup.php`.
3. Plug in phone via USB cable and run [`tools/adb_reverse.bat`](../tools/adb_reverse.bat) to activate USB port tunneling (`adb reverse tcp:8080 tcp:80`).
4. Plug in Arduino Uno on **COM5** and launch [`serial/start_reader.bat COM5`](../serial/start_reader.bat). *(Ensure Arduino IDE Serial Monitor is closed)*.
5. Launch the Android App on your phone and log in with `admin` / `admin123`.

---

## Completed Tasks ✅

- [x] **Project Setup**: Jetpack Compose, Material 3, Retrofit 2 + Gson.
- [x] **MySQL Database (`motion_monitoring`)**:
  - `motion_logs` table (`id`, `event_type`, `zone`, `source`, `detected_at`, `created_at`)
  - `sms_logs` table (`id`, `zone`, `recipient`, `status`, `detail`, `sent_at`, `created_at`)
  - `users` table (`id`, `username`, `email`, `password_hash`, `security_question`, `security_answer_hash`)
  - `sensor_zones` table (`id`, `sensor_code`, `name`, `is_enabled`)
- [x] **PHP REST API (`api/`)**:
  - `config.php` — Connected to `motion_monitoring` database
  - `setup.php` — 1-click schema & default admin table creation
  - `login.php` — Authentication returning clean JSON
  - `log_motion_event.php` — Accepts serial events and saves to `motion_logs`
  - `get_motion_events.php` — Serves multi-zone status and history to Android app
  - `get_sensors.php` / `toggle_sensor.php` — Sensor zone management
- [x] **Arduino Hardware & Firmware (COM5)**:
  - Sketch: [`arduino/poultry_sensor/poultry_sensor.ino`](../arduino/poultry_sensor/poultry_sensor.ino)
  - 3x PIR sensors (Pins 2, 3, 4), local farm alarm buzzer (Pin 8), SIM800L GSM module (Pins 10/11/12).
- [x] **Serial Reader Bridge**:
  - Listener: [`serial/serial_reader.ps1`](../serial/serial_reader.ps1) & [`serial/start_reader.bat COM5`](../serial/start_reader.bat) listening on COM5.
- [x] **Android Application Features**:
  - `MotionStatusCard`: Visual green/red intrusion status banner.
  - `ZoneStatusGrid`: Multi-zone status cards (`Coop Zone A`, `Coop Zone B`, `Coop Zone C`, `Perimeter Gate`).
  - **3-Second Background Polling Loop**: Live automatic updates on motion events.
- [x] **Troubleshooting & Fixes Completed**:
  - `Failed to open COM5`: Solved with native PowerShell .NET serial listener.
  - `JsonReader.setLenient(true)`: Solved by adding missing tables to `motion_monitoring` & returning clean JSON.
  - `Failed to connect to localhost/127.0.0.1:8080`: Solved with `tools/adb_reverse.bat` automated port forwarding.
