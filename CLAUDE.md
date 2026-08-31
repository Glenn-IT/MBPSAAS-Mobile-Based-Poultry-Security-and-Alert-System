# MBPSAAS — Project Memory & Development Notes

Mobile-Based Poultry Security and Alert System (MBPSAAS).
Android (Kotlin/Jetpack Compose) mobile app + Arduino hardware (PIR & SIM800L) + PHP/MySQL backend on XAMPP.

---

## ⚡ CRITICAL SYSTEM MEMORY: Synced with `C:\xampp\htdocs\ABMDMS\`

Whenever modifications or additions are made to the **Arduino Setup**, **PIR Sensors**, **GSM SIM800L**, or **Database** in `C:\xampp\htdocs\ABMDMS\`, the Android project **MUST** be kept in synchronization.

### Key Shared Components:
1. **Database:** Shared single MySQL database **`motion_monitoring`** on XAMPP (`localhost`, user `root`, no password). Tables: `motion_logs`, `sms_logs`, `users`, `sensor_zones`.
2. **Zone Tokens & Pins:**
   - `Pin 2` -> `ROOMC` (Coop Zone C)
   - `Pin 3` -> `ROOMA` (Coop Zone A)
   - `Pin 4` -> `ROOMB` (Coop Zone B)
   - `Pin 8` -> Farm Alarm Buzzer
   - `Pins 10/11/12` -> SIM800L GSM Module
3. **If Arduino or Pins are modified in `htdocs\ABMDMS`**:
   - Synchronize `arduino/poultry_sensor/poultry_sensor.ino`
   - Synchronize `serial/serial_reader.ps1` ($ZonePattern regex & baud rate)
   - Update `data/ApiModels.kt` & `ui/DashboardScreen.kt` in Android Studio if zone names/cards change.
4. **Auto-Sync Helper:** Run `tools\sync_with_htdocs.bat` to check and sync files between `ABMDMS` and this project.

See full details in [`SYSTEM_MEMORY.md`](SYSTEM_MEMORY.md).

---

## Architecture & Data Flow

```
Arduino (3x PIR + SIM800L) 
  ──(COM5 USB)──► PowerShell Bridge (serial_reader.ps1) 
  ──(HTTP POST)──► PHP API (log_motion_event.php) 
  ──(PDO)──► MySQL (motion_monitoring) 
  ──(HTTP GET via ADB 8080)──► Android App (Jetpack Compose 3s polling)
```

---

## File Structure Reference
- **Android App:** `app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/`
  - `data/ApiClient.kt` — Base URL: `http://localhost:8080/mbpsaas_api/`
  - `data/ApiService.kt` — Retrofit endpoints (`get_motion_events.php`, `login.php`, etc.)
  - `data/ApiModels.kt` — Multi-zone response models (`MotionEventsResponse`, `ZoneStatus`)
  - `ui/DashboardScreen.kt` — Live polling (3s loop), farm status card, zone status grid
- **Backend API:** `api/` (linked into XAMPP at `C:\xampp\htdocs\mbpsaas_api\`)
- **Arduino Firmware:** `arduino/poultry_sensor/poultry_sensor.ino`
- **Serial Bridge:** `serial/serial_reader.ps1` & `serial/start_reader.bat`
- **USB Tunnel Script:** `tools/adb_reverse.bat`
- **Sync Tool:** `tools/sync_with_htdocs.bat`
