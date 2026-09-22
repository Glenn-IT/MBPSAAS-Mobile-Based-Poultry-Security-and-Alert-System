# SYSTEM MEMORY — ABMDMS & MBPSAAS Dual-Project Architecture

## 1. Project Locations & Roles

| Project Name | Path | Primary Role |
|---|---|---|
| **ABMDMS (Primary Backend & Hardware Hub)** | `C:\xampp\htdocs\ABMDMS\` | - Hardware Arduino sketch & pin wiring development<br>- SIM800L GSM SMS module configuration<br>- Central MySQL Database (`motion_monitoring`)<br>- Web monitoring dashboard & PHP APIs |
| **MBPSAAS (Android Mobile App Client)** | `C:\Users\GLENN\AndroidStudioProjects\MBPSAASMobileBasedPoultrySecurityandAlertSystem\` | - Android Kotlin / Jetpack Compose mobile application<br>- Retrofit API client (`ApiClient.kt`, `ApiService.kt`)<br>- Replicated Arduino firmware (`poultry_sensor.ino`)<br>- Local serial reader bridge (`serial_reader.ps1`) |
| **XAMPP API Junction** | `C:\xampp\htdocs\mbpsaas_api\` | Direct link / deployment of the Android app's PHP backend connecting to `motion_monitoring` DB. |

---

## 2. The Shared Core (Must Always Stay Synchronized)

Whenever an update is made in `C:\xampp\htdocs\ABMDMS\`, the corresponding component in `MBPSAASMobileBasedPoultrySecurityandAlertSystem` must be updated:

```
┌─────────────────────────────────────────────────────────────┐
│               SHARED DATABASE: motion_monitoring             │
│               - motion_logs (id, event_type, zone, source)  │
│               - sms_logs (id, zone, recipient, status)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
       ┌───────────────────────┴───────────────────────┐
       ▼                                               ▼
┌──────────────────────────────┐        ┌──────────────────────────────┐
│  C:\xampp\htdocs\ABMDMS\     │        │  Android Studio (MBPSAAS)    │
│  - arduino/motion_sensor.ino │ ◄────► │  - arduino/poultry_sensor.ino│
│  - serial/serial_reader.ps1  │ (SYNC) │  - serial/serial_reader.ps1  │
│  - config.php                │        │  - app/.../ApiModels.kt      │
│  - api/                      │        │  - app/.../DashboardScreen.kt│
└──────────────────────────────┘        └──────────────────────────────┘
```

---

## 3. Synchronization Rules & Checklist

### ⚠️ Rule 1: Zone Names, Network IP & Pin Assignments
- **Hardware Pin Map (ESP32 Dev Module):**
  - `GPIO 13`: `ROOMC` ("Coop Zone C" / Room C PIR)
  - `GPIO 12`: `ROOMA` ("Coop Zone A" / Room A PIR)
  - `GPIO 14`: `ROOMB` ("Coop Zone B" / Room B PIR)
  - `GPIO 16 / 17 / 4`: SIM800L GSM Module (RX2 / TX2 / RST)
  - `GPIO 25 / 26 / 27`: MAX98357A I2S Audio Amplifier (LRC/WS, BCLK, DIN)
- **Legacy Pin Map (Arduino Uno):**
  - `Pin 2`: `ROOMC`, `Pin 3`: `ROOMA`, `Pin 4`: `ROOMB`, `Pin 8`: Buzzer, `Pins 10/11/12`: SIM800L
- **Mobile Network Connection (`ApiClient.kt`):**
  - **Wi-Fi Mode**: Set `BASE_URL = "http://<LAPTOP_IP>/mbpsaas_api/"` (e.g. `10.192.10.14`). Phone and Laptop must be on the same Wi-Fi.
  - **USB Cable Mode**: Set `BASE_URL = "http://localhost:8080/mbpsaas_api/"` and run `adb reverse tcp:8080 tcp:80`.
  - 📖 **Full Guide**: See [docs/WIFI_AND_IP_CONFIGURATION_GUIDE.md](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/docs/WIFI_AND_IP_CONFIGURATION_GUIDE.md) for switching Wi-Fi networks.
- **Must update in Android Studio:**
  1. `app/src/main/java/.../data/ApiClient.kt` (`BASE_URL` with current Laptop Wi-Fi IP).
  2. `arduino/poultry_sensor/poultry_sensor.ino` / ESP32 sketch.
  3. `api/config.php` (`ALLOWED_ZONES` and `ZONE_LABELS`).
  4. `app/src/main/java/.../data/ApiModels.kt` (`ZoneMap` or zone models).
  5. `app/src/main/java/.../ui/DashboardScreen.kt` & `HomeScreen.kt` (UI zone cards).

### ⚠️ Rule 2: Database Schema (`motion_monitoring`)
Both projects write to and read from the single MySQL database `motion_monitoring`.
- Any new column or table created in `ABMDMS/database/database.sql` must be supported in `mbpsaas_api/` and `app/src/main/java/.../data/ApiModels.kt`.

### ⚠️ Rule 3: Serial Tokens
- Arduino prints: `ROOMA_MOTION_DETECTED`, `ROOMA_MOTION_STOPPED`, `SMS_SENT:ROOMA`, etc.
- If event string format changes, update `serial_reader.ps1` in both repositories immediately.

---

## 4. 1-Click Synchronization Tool

To check for differences and synchronize firmware/scripts between `htdocs\ABMDMS` and this Android Studio project, run:
```cmd
tools\sync_with_htdocs.bat
```
or in PowerShell:
```powershell
.\tools\sync_with_htdocs.ps1 -Compare
.\tools\sync_with_htdocs.ps1 -Sync
```
