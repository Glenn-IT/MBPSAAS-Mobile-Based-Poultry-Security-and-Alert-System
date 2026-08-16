# MBPSAAS — Arduino Integration Plan (Using ABMDMS `motion_monitoring` Database)

**Status:** APPROVED & UPDATED PLAN — Ready for Implementation.  
**Reference & Database Base:** ABMDMS (`C:\xampp\htdocs\ABMDMS`) — Using database `motion_monitoring` (`motion_logs` & `sms_logs` tables).  
**Target Project:** MBPSAAS (`C:\Users\GLENN\AndroidStudioProjects\MBPSAASMobileBasedPoultrySecurityandAlertSystem`).  
**Decision Taken:** Directly use the **ABMDMS MySQL Database (`motion_monitoring`)** with multi-zone support (`ROOMA`, `ROOMB`, `ROOMC`, optional `ROOMD` / `COOP` mapping) and connect both Arduino hardware and the Android Compose app to it.

---

## 1. Goal

MBPSAAS has the display layer (Jetpack Compose Android app). This plan connects the Android app to the **ABMDMS hardware & database pipeline** (`motion_monitoring` DB -> `motion_logs` table), adding physical PIR motion sensors, local farm buzzer alert, and automated SIM800L SMS alert functionality.

```
┌──────────┐  USB Serial  ┌──────────────────┐  HTTP POST   ┌─────────────────────┐  SQL  ┌─────────────────────┐
│ Arduino  │ ───────────► │ serial_reader.php│ ───────────► │ log_motion_event.php│ ────► │ MySQL Database      │
│ N×PIR +  │  Tokens      │ (PHP Bridge)     │  localhost   │ (Insert Endpoint)   │  PDO  │ `motion_monitoring` │
│ Buzzer/  │              └──────────────────┘              └──────────┬──────────┘       │ (`motion_logs`)     │
│ SIM800L  │                                                           │                      └──────────┬──────────┘
└──────────┘                                                           │  HTTP GET                       │
                                   ┌─────────────────────┐             │                                 │
                                   │  MBPSAAS Android    │◄────────────┘                                 │
                                   │  App (Kotlin)       │  polls get_motion_events.php ─────────────────┘
                                   └─────────────────────┘
```

---

## 2. Database Architecture (Using ABMDMS `motion_monitoring`)

As decided, **MBPSAAS will directly use the ABMDMS MySQL Database (`motion_monitoring`)**.

### Primary Table: `motion_logs`
| Column | Data Type | Constraint | Description |
|---|---|---|---|
| `id` | `INT UNSIGNED` | `AUTO_INCREMENT PK` | Event Unique ID |
| `event_type` | `VARCHAR(20)` | `NOT NULL` | `MOTION_DETECTED` or `MOTION_STOPPED` |
| `zone` | `VARCHAR(20)` | `NOT NULL DEFAULT 'ROOMC'` | Zone code (`ROOMA`, `ROOMB`, `ROOMC`, `ROOMD` or `COOP1-4`) |
| `source` | `VARCHAR(50)` | `NOT NULL DEFAULT 'ARDUINO_PIR'` | `ARDUINO_PIR` or `SIMULATOR` |
| `detected_at` | `DATETIME` | `NOT NULL` | Event Detection Timestamp |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Record Creation Timestamp |

### Secondary Table: `sms_logs`
| Column | Data Type | Constraint | Description |
|---|---|---|---|
| `id` | `INT UNSIGNED` | `AUTO_INCREMENT PK` | SMS Record ID |
| `zone` | `VARCHAR(20)` | `NOT NULL` | Associated Zone |
| `recipient` | `VARCHAR(20)` | `NOT NULL` | Recipient Phone Number |
| `status` | `VARCHAR(20)` | `NOT NULL` | `SENT`, `FAILED`, or `SKIPPED` (cooldown) |
| `detail` | `VARCHAR(100)` | `DEFAULT ''` | Status details |
| `sent_at` | `DATETIME` | `NOT NULL` | Dispatch Timestamp |

---

## 3. Zone Mapping & Configuration (`api/config.php`)

MBPSAAS map definitions in `api/config.php`:

```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'motion_monitoring'); // Uses ABMDMS database directly
define('DB_USER', 'root');
define('DB_PASS', '');

define('ALLOWED_EVENT_TYPES', ['MOTION_DETECTED', 'MOTION_STOPPED']);
define('ALLOWED_ZONES', ['ROOMA', 'ROOMB', 'ROOMC', 'ROOMD']);
define('ZONE_LABELS', [
    'ROOMA' => 'Coop Zone A',
    'ROOMB' => 'Coop Zone B',
    'ROOMC' => 'Coop Zone C',
    'ROOMD' => 'Perimeter Gate'
]);
date_default_timezone_set('Asia/Manila');
```

---

## 4. API Endpoints

### 4.1 Insert Endpoint: `api/log_motion_event.php`
- Accepts `POST` parameters: `event_type`, `zone`, `source`.
- Validates parameters against `ALLOWED_EVENT_TYPES` and `ALLOWED_ZONES`.
- Inserts row into `motion_monitoring.motion_logs`.
- Returns JSON success response.

### 4.2 Read Endpoint: `api/get_motion_events.php`
- Returns status payload formatted for the MBPSAAS Android App:
```json
{
  "success": true,
  "status": "MOTION_DETECTED",
  "total_events": 42,
  "today_events": 10,
  "last_motion": "2026-08-16 18:30:00",
  "zones": {
    "ROOMA": { "label": "Coop Zone A", "status": "NO_MOTION", "last_motion": "..." },
    "ROOMB": { "label": "Coop Zone B", "status": "MOTION_DETECTED", "last_motion": "..." }
  },
  "events": [
    {
      "id": 1,
      "event_type": "MOTION_DETECTED",
      "zone": "ROOMB",
      "zone_label": "Coop Zone B",
      "source": "ARDUINO_PIR",
      "detected_at": "2026-08-16 18:30:00"
    }
  ]
}
```

---

## 5. Arduino Firmware & Serial Reader

### Arduino Sketch (`arduino/poultry_sensor/poultry_sensor.ino`)
- Reads HC-SR501 PIR sensors on pins D2, D3, D4 (and optional D5).
- Drives a local active buzzer on pin D8 whenever any zone is active.
- Communicates with SIM800L GSM module (pins 10, 11, 12) for automated SMS alerts on motion trigger.
- Outputs USB serial tokens: `ROOMA_MOTION_DETECTED`, `ROOMA_MOTION_STOPPED`, etc.

### Serial Bridge Listener (`serial/serial_reader.php`)
- Listens to active COM port (e.g. `COM3`).
- Parses serial tokens and posts them to `http://localhost/mbpsaas_api/log_motion_event.php`.

---

## 6. Android Mobile App Integration

- **`data/ApiModels.kt`**: Update `MotionEvent` and `MotionEventsResponse` to map `event_type`, `zone`, `zone_label`, `source`, `detected_at`, and `zones` map.
- **`data/ApiService.kt`**: Update `@GET("get_motion_events.php")` call signature.
- **`ui/DashboardScreen.kt`**: Display real-time zone cards (`Coop Zone A`, `Coop Zone B`, etc.), overall security status badge (Clear / Intrusion Detected), and motion history list.
- **`ui/HomeScreen.kt`**: Configure periodic 3-second auto-refresh polling loop.

---

## 7. Implementation File Checklist

| Action | Path | Description |
|---|---|---|
| Create | `api/config.php` | Configured with `motion_monitoring` database parameters. |
| Update | `api/log_motion_event.php` | Inserts validated events into `motion_logs`. |
| Update | `api/get_motion_events.php` | Delivers multi-zone JSON payload to Android app. |
| Create | `tools/simulate_motion.php` | Web tool to simulate sensor triggers without hardware. |
| Create | `arduino/poultry_sensor/poultry_sensor.ino` | Arduino sketch for PIR + Buzzer + SIM800L. |
| Create | `serial/serial_reader.php` | Serial bridge feeding `motion_logs`. |
| Update | `app/.../data/ApiModels.kt` | Android data models aligned with `motion_logs` schema. |
| Update | `app/.../ui/DashboardScreen.kt` | Jetpack Compose UI showing live zone security status. |
