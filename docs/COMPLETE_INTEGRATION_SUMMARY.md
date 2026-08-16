# MBPSAAS — Complete Integration & System Documentation

## 1. Executive Summary

The **Mobile-Based Poultry Security and Alert System (MBPSAAS)** is an end-to-end multi-zone intrusion detection, local audio alarm, cellular SMS notification, and mobile Android monitoring solution. 

This document records the full implementation, architecture, database design, troubleshooting fixes, and operating instructions for the integrated system.

---

## 2. System Architecture & Data Flow

```
+--------------------------------+
| Arduino Uno Microcontroller    |
| - PIR Sensor 1 (Pin 2: ROOMC)  |
| - PIR Sensor 2 (Pin 3: ROOMA)  |
| - PIR Sensor 3 (Pin 4: ROOMB)  |
| - Alarm Buzzer (Pin 8)         |
| - SIM800L GSM (Pins 10/11/12)  |
+---------------+----------------+
                |
          (USB Serial COM5)
                v
+---------------+----------------+
| Serial Bridge Listener         |
| (serial_reader.ps1 / COM5)     |
+---------------+----------------+
                |
           (HTTP POST)
                v
+---------------+----------------+       +------------------------------------+
| XAMPP Apache REST API          | ----> | MySQL Database (`motion_monitoring`)|
| (log_motion_event.php)         |  PDO  | - motion_logs   - sms_logs         |
+---------------+----------------+       | - users         - sensor_zones     |
                |                        +------------------------------------+
           (HTTP GET)
                v
+---------------+----------------+
| Android App (Kotlin/Compose)   |
| (USB ADB Tunnel: port 8080)    |
+--------------------------------+
```

---

## 3. Component Details & Setup

### 3.1 Database Architecture (`motion_monitoring`)
The system directly utilizes the MySQL database **`motion_monitoring`**:

- **`motion_logs`**: Stores intrusion events (`id`, `event_type`, `zone`, `source`, `detected_at`, `created_at`).
- **`sms_logs`**: Stores cellular text alert dispatch records (`id`, `zone`, `recipient`, `status`, `detail`, `sent_at`).
- **`users`**: Stores user authentication records (default account: `admin` / `admin123`).
- **`sensor_zones`**: Manages sensor zone definitions (`ROOMA`: Coop Zone A, `ROOMB`: Coop Zone B, `ROOMC`: Coop Zone C, `ROOMD`: Perimeter Gate).

### 3.2 Hardware Configuration (Arduino Uno on COM5)
- **PIR Sensor 1 OUT**: Connected to **Digital Pin 2** (`ROOMC` / Coop Zone C).
- **PIR Sensor 2 OUT**: Connected to **Digital Pin 3** (`ROOMA` / Coop Zone A).
- **PIR Sensor 3 OUT**: Connected to **Digital Pin 4** (`ROOMB` / Coop Zone B).
- **Farm Alarm Buzzer (+)**: Connected to **Digital Pin 8** (Sounds whenever any zone is active).
- **SIM800L GSM Module**: SoftwareSerial TX **Pin 10**, RX **Pin 11**, RST **Pin 12** powered via **External 5V/2A supply** with a **1000µF capacitor** across power terminals.

### 3.3 Serial Reader Bridge (`serial/`)
- **Script**: `serial/serial_reader.ps1` (PowerShell .NET SerialPort bridge) & `serial/start_reader.bat`.
- **Target Port**: `COM5` at 9600 baud.
- **Function**: Listens to serial tokens (`ROOMA_MOTION_DETECTED`, `ROOMA_MOTION_STOPPED`) and forwards them via HTTP POST to `http://localhost/mbpsaas_api/log_motion_event.php`.

### 3.4 Android Application (`app/`)
- **Framework**: Kotlin & Jetpack Compose.
- **Networking**: Retrofit 2 & Gson Converter.
- **Base URL**: `http://localhost:8080/mbpsaas_api/` via USB cable tunneling.
- **Features**:
  - `MotionStatusCard`: Displays overall farm security state (**ALL POULTRY ZONES SAFE** vs **INTRUSION ALERT**).
  - `ZoneStatusGrid`: Renders real-time status cards for each poultry zone.
  - **3-Second Live Polling Loop**: Automatically updates the UI in real-time when motion is detected.

---

## 4. Key Issues Resolved During Integration

### Issue 1: `Failed to open COM5`
- **Cause**: Windows COM port locking when Arduino IDE Serial Monitor was open, or PHP stream path formatting.
- **Fix**: Created native PowerShell `.NET System.IO.Ports.SerialPort` bridge ([`serial/serial_reader.ps1`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/serial/serial_reader.ps1)) and updated `serial_reader.php` port path string formatting (`COM5:`).

### Issue 2: `JsonReader.setLenient(true) ... malformed JSON at line 1 column 1`
- **Cause**: PHP script threw an unhandled HTML fatal error (`<br /><b>Fatal error: Table 'motion_monitoring.users' doesn't exist</b>`), returning HTML instead of JSON to Retrofit.
- **Fix**: Updated [`api/setup.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/setup.php) to instantiate `users`, `sensor_zones`, `motion_logs`, and `sms_logs` in `motion_monitoring`, and added safe JSON error handling in [`api/login.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/login.php).

### Issue 3: `Failed to connect to localhost/127.0.0.1:8080`
- **Cause**: Android phone cannot access PC `localhost` without active ADB port forwarding.
- **Fix**: Created **[`tools/adb_reverse.bat`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/adb_reverse.bat)** script executing `adb reverse tcp:8080 tcp:80` for 1-click USB tunneling.

---

## 5. System File Reference

| Purpose | File Path | Description |
|---|---|---|
| **Database Setup** | [`api/setup.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/setup.php) | Instantiates all database tables in `motion_monitoring`. |
| **API Configuration** | [`api/config.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/config.php) | DB credentials and zone definitions. |
| **Event Logging API** | [`api/log_motion_event.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/log_motion_event.php) | Receives POST events and writes to `motion_logs`. |
| **Event Read API** | [`api/get_motion_events.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/get_motion_events.php) | Serves multi-zone JSON payload to Android app. |
| **Web Simulator** | [`tools/simulate_motion.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/simulate_motion.php) | Web tool to test motion events without hardware. |
| **USB Tunnel Script** | [`tools/adb_reverse.bat`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/adb_reverse.bat) | Activates `adb reverse tcp:8080 tcp:80` for USB debugging. |
| **Serial Listener** | [`serial/start_reader.bat`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/serial/start_reader.bat) | Launches PowerShell serial listener on COM5. |
| **Arduino Firmware** | [`arduino/poultry_sensor/poultry_sensor.ino`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/arduino/poultry_sensor/poultry_sensor.ino) | Arduino C++ firmware for PIR + Buzzer + SIM800L. |
| **Android API Client** | [`app/.../data/ApiClient.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/data/ApiClient.kt) | Retrofit client set to `http://localhost:8080/mbpsaas_api/`. |
| **Android Data Models** | [`app/.../data/ApiModels.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/data/ApiModels.kt) | Gson models for multi-zone data structures. |

---

## 6. Daily Operating Guide

1. **Start Backend**: Ensure Apache and MySQL are running in XAMPP.
2. **Connect Phone**: Plug in Android phone via USB and run [`tools/adb_reverse.bat`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/adb_reverse.bat).
3. **Start Arduino Listener**: Plug in Arduino Uno to COM5 and run [`serial/start_reader.bat COM5`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/serial/start_reader.bat). *(Ensure Arduino IDE Serial Monitor is closed)*.
4. **Launch Android App**: Open the app and log in using **`admin`** / **`admin123`**.
