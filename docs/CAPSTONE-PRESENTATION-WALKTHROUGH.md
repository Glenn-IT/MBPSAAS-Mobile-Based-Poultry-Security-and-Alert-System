# MBPSAAS — Capstone Defense Presentation Walkthrough & Panelist Demonstration Guide
<!-- System: Mobile-Based Poultry Security and Alert System (MBPSAAS) -->
<!-- Client: Native Android Application (Kotlin, Jetpack Compose, Material Design 3) -->
<!-- Hardware: ESP32 Dev Module / Arduino Uno, HC-SR501 PIR Sensors, SIM800L GSM Module, Siren/Buzzer -->
<!-- Backend: PHP RESTful API (`mbpsaas_api` / `ABMDMS`), MySQL/MariaDB (`motion_monitoring`), PowerShell Bridge -->
<!-- Target Audience: Capstone Defense Panelists, Advisers, Technical Evaluators, and Poultry Farm Operators -->

---

## 🧭 Executive Summary & Timing Strategy

| Phase | Section | Recommended Duration | Primary Interface / Component |
| :--- | :--- | :--- | :--- |
| **Phase 1** | Project Rationale, Poultry Farming Context & Problem Statement | 1.5 mins | Title Slide / System Overview / Farm Diagram |
| **Phase 2** | Technical Architecture, Tri-Tier Ecosystem & Hardware-Software Bridge | 1.5 mins | [docs/COMPLETE_INTEGRATION_SUMMARY.md](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/docs/COMPLETE_INTEGRATION_SUMMARY.md) & [SYSTEM_MEMORY.md](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/SYSTEM_MEMORY.md) |
| **Phase 3** | Farm Security Authentication, Role Governance & Self-Service Account Recovery | 1.0 min | [LoginScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/LoginScreen.kt) & [ForgotPasswordScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ForgotPasswordScreen.kt) |
| **Phase 4** | Mobile Command Dashboard, Zone Status Grid & 3-Second Reactive Polling | 1.5 mins | [DashboardScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/DashboardScreen.kt) |
| **Phase 5** | Live Multi-Zone Intrusion Detection & Local Audio Deterrence Demonstration | 2.0 mins | Hardware PIR / ESP32 / Serial Bridge & [tools/simulate_motion.php](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/simulate_motion.php) |
| **Phase 6** | Autonomous Cellular SMS Alert Dispatch (SIM800L GSM Pipeline) | 1.5 mins | Physical GSM Recipient Phone & `sms_logs` Database Table |
| **Phase 7** | Remote Zone Control: Mobile Sensor Arming & Disarming Controls | 1.0 min | [SensorControlCard](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/MotionEventComponents.kt) in `DashboardScreen.kt` |
| **Phase 8** | Triggered Alerts Incident Feed, Date Filtering & Zone Segmentation | 1.5 mins | [TriggeredAlertsScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/TriggeredAlertsScreen.kt) |
| **Phase 9** | Full Farm Security Audit Trail & Raw Chronological Activity Logs | 1.0 min | [ActivityLogScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ActivityLogScreen.kt) |
| **Phase 10** | Farm Operator Credential Governance & Profile Maintenance | 0.5 min | [ProfileScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ProfileScreen.kt) |
| **Phase 11** | Fail-Safe Architecture, Redundancy Protocols & Technical Defense Summary | 1.0 min | Architecture Slide / Verification Summary |
| **Total** | **Full System Defense Presentation** | **~14.0 mins** | — |

---

## 🛠️ Pre-Defense Staging & Demonstration Setup

To ensure an uninterrupted, fail-safe live demonstration in front of the panelists, prepare your demonstration hardware and software workstation beforehand:

### 1. Workstation & Server Configuration
* **Local Web & Database Server:** Ensure **Apache** and **MySQL** are running in the XAMPP Control Panel.
* **Database Verification:** Check that the MySQL database `motion_monitoring` is active with seeded tables: `motion_logs`, `sms_logs`, `users`, and `sensor_zones` (instantiated via [`api/setup.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/setup.php)).
* **Simulated Web Trigger Backup:** Keep [`tools/simulate_motion.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/simulate_motion.php) open in a browser tab. This serves as an immediate software fallback should physical sensor hardware or USB ports experience physical disconnection.

### 2. Microcontroller & Sensor Hardware Wiring
* **Board:** ESP32 Dev Module (or Arduino Uno on COM5).
* **PIR Sensor Zone Mapping:**
  * **GPIO 12 (Pin 3 on Uno):** `ROOMA` — **Coop Zone A** (Brooder / Layer Pen)
  * **GPIO 14 (Pin 4 on Uno):** `ROOMB` — **Coop Zone B** (Grower Pen)
  * **GPIO 13 (Pin 2 on Uno):** `ROOMC` — **Coop Zone C** (Feed Storage & Free-Range Perimeter)
* **Audio Deterrence:** Passive Buzzer / Siren (+) connected to **GPIO 25** (Pin 8 on Uno).
* **Cellular GSM:** SIM800L module connected to Hardware Serial UART2 (**GPIO 16 RX2 / GPIO 17 TX2 / GPIO 4 RST**). Ensure external 5V/2A power supply with a 1000µF capacitor across VCC and GND.
* **Serial Reader Bridge (if using Uno / Serial USB):** Open PowerShell terminal and launch [`serial/start_reader.bat COM5`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/serial/start_reader.bat) *(ensure Arduino Serial Monitor is closed)*.

### 3. Android Mobile Device Setup
* **Device Screen Mirroring:** Connect the Android phone via USB and run `scrcpy` (or Android Studio Device Mirroring) so panelists can clearly see the live phone screen projected on the presentation display.
* **ADB Network Reverse Tunnel:** Run [`tools/adb_reverse.bat`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/adb_reverse.bat) in command prompt (`adb reverse tcp:8080 tcp:80`).
* **Connection Mode:**
  * **USB Tethered Mode (Recommended for Defense):** `BASE_URL = "http://localhost:8080/mbpsaas_api/"` in [`ApiClient.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/data/ApiClient.kt). Immune to venue Wi-Fi drops!
  * **Wi-Fi Mode (Alternative):** `BASE_URL = "http://<LAPTOP_IP>/mbpsaas_api/"` where laptop and mobile phone share the same hotspot/Wi-Fi.

### 👥 Seeded Farm Operator Demonstration Accounts

| Role | Username | Password | Security Question & Answer | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Farm Master Administrator** | `admin` | `admin123` | *"What is your favorite animal?"* &rarr; `chicken` | Full access to farm dashboard, zone toggles, incident logs, and profile settings. |
| **Secondary Farm Overseer** | `farm_operator` | `sample123` | *"What is your favorite food?"* &rarr; `poultry` | Operator-level account for testing forgot password and credential isolation. |

### 🐔 Seeded Poultry Farm Sensor Zones

| Zone ID | Farm Zone Name | Monitored Poultry Area | Installed Hardware | Active Alert Action |
| :--- | :--- | :--- | :--- | :--- |
| `ROOMA` | **Coop Zone A** | Chick Brooder & High-Value Layer Pens | HC-SR501 PIR (GPIO 12) | Local Siren, Instant Push/UI Banner, SMS to Operator |
| `ROOMB` | **Coop Zone B** | Grower Flock & Feed Troughs | HC-SR501 PIR (GPIO 14) | Local Siren, Instant Push/UI Banner, SMS to Operator |
| `ROOMC` | **Coop Zone C** | Feed Storage & Outer Perimeter Gate | HC-SR501 PIR (GPIO 13) | Local Siren, Instant Push/UI Banner, SMS to Operator |

---

## 🎬 Step-by-Step Presentation Script (From First to Last)

---

### Step 1: Project Rationale, Poultry Farming Context & Problem Statement
* **Screen Display:** Title Slide / System Overview / Farm Diagram
* **Estimated Time:** 1.5 minutes
* **Screen Action:** Present the title slide showing the agricultural relevance of poultry farming in the Philippines and the critical vulnerabilities faced by farm operators.
* **🗣️ Verbal Script:**
  > *"Good morning, honorable members of the panel, our respected capstone adviser, and guests. Today, we are proud to present **MBPSAAS — the Mobile-Based Poultry Security and Alert System**.
  >
  > *Poultry farming represents a vital component of the Philippine agricultural economy, supplying essential food security and livelihood to thousands of smallholder and commercial raisers. However, poultry farms face significant, recurring security threats: nocturnal livestock theft or 'salisi', predatory intrusions by feral dogs, cats, rodents, and wild snakes, and unauthorized trespassing into feed storage facilities.
  >
  > *Traditionally, poultry farmers rely on manual night watches, perimeter fences, or perimeter lighting. These traditional measures fail because farm owners cannot physically guard multiple coop buildings 24 hours a day, especially during late-night and pre-dawn hours when intrusions occur. When an intrusion happens, the farmer discovers the mortality or theft hours too late.
  >
  > *MBPSAAS addresses this problem by delivering a low-latency, tri-tier security ecosystem. By combining multi-zone passive infrared motion detection, local siren deterrence, cellular GSM SMS notifications, and a modern Android Jetpack Compose monitoring dashboard, MBPSAAS gives farm operators complete, real-time awareness and defensive control directly from their smartphone."*

---

### Step 2: Technical Architecture, Tri-Tier Ecosystem & Hardware-Software Bridge
* **Screen Display:** Architecture Diagram / [COMPLETE_INTEGRATION_SUMMARY.md](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/docs/COMPLETE_INTEGRATION_SUMMARY.md) & [SYSTEM_MEMORY.md](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/SYSTEM_MEMORY.md)
* **Estimated Time:** 1.5 minutes
* **Screen Action:** Walk the panel through the tri-tier architecture connecting the physical microcontroller, the central server, and the mobile client.
* **🗣️ Verbal Script:**
  > *"To ensure maximum reliability even in remote rural farms where internet connectivity is intermittent, MBPSAAS is structured into three tightly synchronized architectural tiers:
  >
  > 1. **Hardware Detection & Deterrence Tier:** Built on an ESP32 microcontroller (or Arduino Uno) wired to three precision HC-SR501 PIR motion sensors covering Coop Zones A, B, and C. The firmware incorporates hardware debouncing, a 30-second sensor warmup cycle, and a 500ms confirmation window to eliminate false positives caused by minor air currents. When motion is verified, the board immediately triggers a high-decibel acoustic siren and commands the SIM800L GSM transceiver over hardware UART.
  > 2. **Central RESTful API & Relational Database Tier:** Powered by PHP 8.2 and MariaDB MySQL (`motion_monitoring`). The backend records every event into immutable `motion_logs` and tracks cellular message dispatches in `sms_logs`. It features multi-zone aggregation, security question validation, and JSON REST endpoints (`api/get_motion_events.php`, `api/toggle_sensor.php`).
  > 3. **Native Android Client Tier:** Developed in Kotlin using modern Jetpack Compose and Material Design 3. Communicating via Retrofit 2 and Gson serialization, the app runs a reactive 3-second background polling cycle that continuously synchronizes farm status without draining device resources."*

---

### Step 3: Farm Security Authentication, Role Governance & Self-Service Account Recovery
* **Screen Display:** Android Device: [LoginScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/LoginScreen.kt) & [ForgotPasswordScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ForgotPasswordScreen.kt)
* **Estimated Time:** 1.0 minute
* **Screen Action:**
  1. Showcase the clean mobile Login screen with username and secure password inputs (utilizing [`PasswordField.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/PasswordField.kt) with visibility toggle).
  2. Tap **"Forgot Password?"** to transition to [`ForgotPasswordScreen.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ForgotPasswordScreen.kt).
  3. Enter username `admin` and demonstrate the 3-step credential recovery workflow:
     * Step 1: User Identity Verification.
     * Step 2: Challenge-Response Security Question matching the account.
     * Step 3: Password Reset confirmation.
  4. Return to Login and authenticate using `admin` / `admin123`.
* **🗣️ Verbal Script:**
  > *"Because poultry farm security systems manage active alarms and perimeter controls, unauthorized access must be strictly prevented.
  >
  > *Our Android client implements a secure authentication layer backed by SHA-256 hashed credentials and parameterized SQL queries in [api/login.php](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/login.php).
  >
  > *Should an operator forget their password while in the field, our 3-step self-service recovery system in [ForgotPasswordScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ForgotPasswordScreen.kt) verifies personal challenge questions against the database, enabling secure, autonomous credential recovery without requiring manual database administration."*

---

### Step 4: Mobile Command Dashboard, Zone Status Grid & 3-Second Reactive Polling
* **Screen Display:** Android Device: [DashboardScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/DashboardScreen.kt)
* **Estimated Time:** 1.5 minutes
* **Screen Action:**
  1. Present the Dashboard while all zones are secure.
  2. Point out the top **MotionStatusCard** showing the green **"ALL POULTRY ZONES SAFE"** banner with a shield icon.
  3. Highlight the **ZoneStatusGrid** displaying three distinct poultry zone cards:
     * **Coop Zone A** (Brooder Pen) &rarr; Safe / Inactive.
     * **Coop Zone B** (Grower Pen) &rarr; Safe / Inactive.
     * **Coop Zone C** (Feed Storage & Perimeter) &rarr; Safe / Inactive.
  4. Show the **Recent Intrusion Activity** section displaying the latest 10 timestamped sensor events.
  5. Mention the coroutine lifecycle in [`HomeScreen.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/HomeScreen.kt) actively polling the backend every 3 seconds.
* **🗣️ Verbal Script:**
  > *"Upon logging in, the farmer is greeted by the central **Poultry Security Dashboard**.
  >
  > *At a single glance, the operator sees the high-level farm security state. The top status card prominently shows 'ALL POULTRY ZONES SAFE' when no motion is detected.
  >
  > *Directly below, the Zone Status Grid provides granular visibility into individual farm compartments: Coop Zone A, Coop Zone B, and Coop Zone C. Each card reflects real-time status, hardware pin mappings, and the exact timestamp of the last logged activity.
  >
  > *The app executes an efficient 3-second coroutine polling loop. If an intruder approaches any coop zone, the mobile interface updates almost instantaneously without requiring manual pull-to-refresh gestures."*

---

### Step 5: Live Multi-Zone Intrusion Detection & Local Audio Deterrence Demonstration
* **Screen Display:** Android Dashboard Screen projected side-by-side with physical hardware (or [simulate_motion.php](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/simulate_motion.php))
* **Estimated Time:** 2.0 minutes
* **Screen Action:**
  1. Trigger an intrusion in **Coop Zone A**:
     * *Hardware method:* Wave hand across PIR Sensor A (GPIO 12 / Pin 3).
     * *Software simulation fallback:* Click "Trigger Motion in Coop Zone A" in `simulate_motion.php`.
  2. **Observe Hardware Reaction:**
     * The built-in LED illuminates and the passive buzzer / audio siren sounds its emergency alert pattern to scare away intruders or predators.
  3. **Observe Mobile Screen Reaction:**
     * Within 3 seconds, the top **MotionStatusCard** flips from green to a pulsating red **"INTRUSION ALERT! MOTION DETECTED"** banner.
     * **Coop Zone A** card highlights with an alert badge, showing `ACTIVE INTRUSION DETECTED` with the exact current timestamp.
  4. Wait for motion to cease:
     * Point out how the board waits for the 2000ms stop confirmation window before logging `ROOMA_MOTION_STOPPED`, resetting the system to armed readiness.
* **🗣️ Verbal Script:**
  > *"Now, we will demonstrate a live simulated intrusion in Coop Zone A.
  >
  > *As motion is detected, two defensive layers fire simultaneously:
  >
  > *First, the local farm alarm sounds immediately. This audible deterrence disorients human intruders and frightens away nocturnal predators before birds are harmed.
  >
  > *Second, within 3 seconds, look at the Android screen: the status banner flips to red 'INTRUSION ALERT!', and Coop Zone A immediately highlights with active warning indicators. The farmer instantly knows which specific building is under threat."*

---

### Step 6: Autonomous Cellular SMS Alert Dispatch (SIM800L GSM Pipeline)
* **Screen Display:** Physical GSM recipient mobile phone or camera projection + MySQL `sms_logs` table
* **Estimated Time:** 1.5 minutes
* **Screen Action:**
  1. Show the panel the incoming SMS on the operator's mobile phone:
     * SMS Content: `[ALERT] Motion detected in Coop Zone A at 23:59:12. Inspect coop immediately!`
  2. Explain the microcontroller's GSM state engine:
     * Baud rate: 9600 on Hardware UART2.
     * 60-second cooldown timer per zone to prevent spamming the farmer's SIM card and exhausting prepaid balance.
     * Minimum 5-second gap between successive text dispatches across zones.
  3. Query `sms_logs` in phpMyAdmin or show API logs to demonstrate the recorded dispatch status (`Sent` / `Delivered`).
* **🗣️ Verbal Script:**
  > *"A critical limitation of internet-only smart farm systems is that if rural Wi-Fi drops or the farmer is sleeping without mobile data, push notifications will never arrive.
  >
  > *MBPSAAS overcomes this with its dedicated **SIM800L GSM Cellular Pipeline**.
  >
  > *When an intrusion occurs, the microcontroller communicates with the cellular transceiver via AT commands. An SMS alert is dispatched directly over the 2G cellular network to the farmer's mobile number.
  >
  > *To protect against message flooding and prepaid load exhaustion, our firmware enforces an intelligent 60-second per-zone cooldown mechanism. Even in deep rural areas with zero internet, the farmer is guaranteed an urgent text alert."*

---

### Step 7: Remote Zone Control: Mobile Sensor Arming & Disarming Controls
* **Screen Display:** Android Device: **SensorControlCard** in [DashboardScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/DashboardScreen.kt)
* **Estimated Time:** 1.0 minute
* **Screen Action:**
  1. Scroll down to the **"Sensor Zone Controls"** card on the Dashboard.
  2. Locate the toggle switch for **Coop Zone B**.
  3. Toggle the switch to **Disabled / Disarmed**:
     * Point out the optimistic UI update and the backend call to [`api/toggle_sensor.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/toggle_sensor.php).
     * Notice how the status badge updates to `Disabled / Off`.
  4. Explain the practical farming use-case: Disarming a zone during scheduled flock feeding, egg collection, or veterinary visits to prevent false alarms.
  5. Toggle the switch back to **Active / Armed**.
* **🗣️ Verbal Script:**
  > *"Farm operations require scheduled human presence during daily routines like morning feeding, egg collection, and veterinary inspections. Constant sirens during these times would cause unnecessary flock panic and stress.
  >
  > *Under the Sensor Controls section, the operator can selectively arm or disarm individual coop zones.
  >
  > *When Zone B is disabled, its detection status is bypassed in [api/toggle_sensor.php](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/toggle_sensor.php). Once chores are completed, the farmer re-arms the zone with a single tap, returning the coop to 24/7 protection."*

---

### Step 8: Triggered Alerts Incident Feed, Date Filtering & Zone Segmentation
* **Screen Display:** Android Device: [TriggeredAlertsScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/TriggeredAlertsScreen.kt)
* **Estimated Time:** 1.5 minutes
* **Screen Action:**
  1. Tap the **"Alerts"** icon in the bottom navigation bar.
  2. Showcase the dedicated incident feed filtering specifically for security breaches (`MOTION_DETECTED`).
  3. Test the **Date Filter Chips**:
     * Tap **"Today"** &rarr; displays only incidents logged on the current calendar date.
     * Tap **"Yesterday"** &rarr; displays previous day's breaches.
     * Tap **"Custom Date"** &rarr; triggers native Android `DatePickerDialog` to select a historical date.
     * Tap **"All Dates"** &rarr; returns to the complete incident history.
  4. Test the **Zone Filter Chips**:
     * Select **Coop Zone A**, **Coop Zone B**, or **Coop Zone C** to isolate intrusions occurring within specific poultry structures.
  5. Point out the incident card metadata: Zone title, timestamp, source (`PIR Sensor` / `ESP32 Wi-Fi`), and alert severity icon.
* **🗣️ Verbal Script:**
  > *"When investigating security incidents or submitting farm theft reports, poultry raisers need filtered, targeted intelligence rather than sifting through thousands of raw records.
  >
  > *Our **Triggered Alerts Screen** automatically filters only confirmed breach events.
  >
  > *Using interactive FilterChips, the operator can isolate intrusions by specific dates—such as 'Today', 'Yesterday', or via a custom date picker—and segment incidents by individual coop buildings. This enables raisers to pinpoint patterns, such as repeated predator attacks occurring at 2:00 AM in Coop Zone C."*

---

### Step 9: Full Farm Security Audit Trail & Raw Chronological Activity Logs
* **Screen Display:** Android Device: [ActivityLogScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ActivityLogScreen.kt)
* **Estimated Time:** 1.0 minute
* **Screen Action:**
  1. Tap the **"Activity Log"** icon on the bottom navigation bar.
  2. Showcase the comprehensive system audit trail containing both `MOTION_DETECTED` and `MOTION_STOPPED` lifecycle events.
  3. Demonstrate the clean UI presentation using [`MotionEventRow`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/MotionEventComponents.kt):
     * Green badges for motion cleared / stopped.
     * Amber/Red badges for motion detected.
     * Formatted human-readable timestamps (`yyyy-MM-dd HH:mm:ss`).
* **🗣️ Verbal Script:**
  > *"For comprehensive audit compliance, the **Activity Log Screen** displays the complete chronological telemetry of the farm.
  >
  > *Unlike the Triggered Alerts screen which focuses solely on alarms, the Activity Log records the exact lifecycle of every event—recording when motion commenced, how long it persisted, and the precise second the sensor verified that the area returned to calm. This provides full forensic clarity during investigations."*

---

### Step 10: Farm Operator Credential Governance & Profile Maintenance
* **Screen Display:** Android Device: [ProfileScreen.kt](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ProfileScreen.kt)
* **Estimated Time:** 0.5 minute
* **Screen Action:**
  1. Tap the **"Profile"** tab on the navigation bar.
  2. Show user profile details: Username (`admin`) and registered email address.
  3. Tap **"Edit Profile"** to reveal credential management fields.
  4. Emphasize that modifying the username, email, or password requires entering the current active password, preventing unauthorized takeover should an unattended device be accessed.
  5. Tap the **Logout** button to show the confirmation dialog, returning securely to the Login screen.
* **🗣️ Verbal Script:**
  > *"Under the **Profile Screen**, farm managers maintain their administrative credentials.
  >
  > *Any update to administrative usernames, email addresses, or access passwords strictly requires verification of the current password in [api/update_profile.php](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/update_profile.php).
  >
  > *Logging out terminates the session and safely restores the device to its locked authentication gateway."*

---

### Step 11: Fail-Safe Architecture, Redundancy Protocols & Technical Defense Summary
* **Screen Display:** Architecture Diagram Slide / System Summary
* **Estimated Time:** 1.0 minute
* **Screen Action:** Conclude the formal presentation by reiterating the dual-communication redundancy (Wi-Fi + Cellular) and software robustness.
* **🗣️ Verbal Script:**
  > *"In summary, the Mobile-Based Poultry Security and Alert System delivers an end-to-end defense ecosystem engineered specifically for the realities of poultry agriculture:
  >
  > *First, **Real-Time Detection & Rapid Deterrence**: Multi-zone PIR sensors coupled with local audio alarms scare away thieves and predators before casualties occur.
  > *Second, **Dual-Channel Alert Redundancy**: If local Wi-Fi or mobile data fails, the SIM800L cellular pipeline guarantees that emergency SMS dispatches reach the owner's phone.
  > *Third, **Modern Native Mobile Ergonomics**: The Jetpack Compose Android client provides live 3-second status updates, zone toggling, and incident filtering in an intuitive interface.
  >
  > *Thank you very much, honorable members of the panel. We are now open and eager to receive your questions."*

---

## 🛡️ Capstone Defense Panelist Q&A Cheat Sheet

| Question | Recommended Technical & Agricultural Defense Answer |
| :--- | :--- |
| **Q1: Why use PIR motion sensors instead of computer vision CCTV cameras with AI object detection?** | *"While CCTV with AI object detection is powerful, it has major drawbacks in agricultural poultry environments: **high cost, high power consumption, and severe vulnerability to outdoor lighting changes, dust, and spiderwebs**. Additionally, streaming video requires continuous high-speed broadband which is rarely available in rural farm coops. HC-SR501 PIR sensors are extremely cost-effective, consume negligible power (making them easily battery/solar backed), operate flawlessly in pitch-black night conditions without supplemental lighting, and can instantly trigger alarms without latency."* |
| **Q2: How does the system prevent false alarms caused by birds fluttering or small rats inside the coop?** | *"We employ a multi-layered filtering strategy: (1) **Hardware Placement**: PIR sensors are installed angled downward at human chest height (approx. 5 to 6 feet) along coop perimeter entryways and aisles, directing detection beams away from floor roosting areas; (2) **Sensitivity Tuning**: Potentiometers on the HC-SR501 adjust detection threshold and delay time; (3) **Firmware Debouncing**: In [`arduino/poultry_sensor.ino`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/arduino/poultry_sensor/poultry_sensor.ino), our code enforces a continuous `START_CONFIRM_MS = 500ms` window—transient electrical spikes or a bird flying across the lens for 100ms are ignored; only continuous thermal body displacement triggers an alarm; and (4) **Zone Controls**: Farm raisers can temporarily disable zones during feeding chores via [`api/toggle_sensor.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/toggle_sensor.php)."* |
| **Q3: What happens if the farm loses Wi-Fi or internet connectivity during the night?** | *"This is precisely why MBPSAAS features **cellular redundancy**. The ESP32/microcontroller communicates directly with an onboard SIM800L GSM module via hardware serial. Even if the local router loses electricity or internet service completely, the microcontroller detects the motion, triggers the local high-decibel buzzer siren to deter the intruder on site, and dispatches an emergency SMS text alert directly through the cellular GSM tower to the owner's phone."* |
| **Q4: How does the system prevent cellular prepaid load exhaustion if a stray animal continuously triggers the sensor?** | *"In our microcontroller firmware ([`arduino/poultry_sensor.ino`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/arduino/poultry_sensor/poultry_sensor.ino)), we implemented an automated **cellular cooldown lock**: `SMS_COOLDOWN_MS = 60000` (60 seconds per zone) and `SMS_MIN_GAP_MS = 5000` (5-second rest between transmissions). Once an SMS is dispatched for Coop Zone A, the GSM engine suppresses further texts for that zone for a full minute while local sirens continue to sound. This guarantees the operator's SIM balance is preserved."* |
| **Q5: Why did you choose Jetpack Compose over traditional Android XML Views?** | *"Jetpack Compose is Android's modern declarative UI framework. It drastically reduces boilerplate code, eliminates synchronization bugs between view states and UI widgets, and simplifies reactive programming. Because our app relies on continuous 3-second data streams, Compose's reactive state system (`remember`, `mutableStateOf`, `LaunchedEffect`) automatically recomposes only the specific zone cards or status banners that changed, resulting in smoother 60fps performance and minimal battery consumption."* |
| **Q6: How does the Android app communicate with the server if the phone is plugged into the PC via USB during testing?** | *"During development and presentation testing, we utilize **ADB Port Reversal** (`adb reverse tcp:8080 tcp:80`) via [`tools/adb_reverse.bat`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/adb_reverse.bat). This routes requests from the Android device's `localhost:8080` port directly through the USB cable to port 80 of the host computer's Apache web server. In live farm deployment, [`ApiClient.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/data/ApiClient.kt) is configured to connect across the local Wi-Fi network (`http://<SERVER_IP>/mbpsaas_api/`) or a cloud-hosted domain."* |
| **Q7: How is data integrity protected against SQL injection attacks in the PHP backend?** | *"All database transactions in `mbpsaas_api` utilize **PHP MySQLi Prepared Statements** with explicit parameter binding (`$stmt->bind_param(...)`) in [`api/login.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/login.php), [`api/reset_password.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/reset_password.php), and [`api/update_profile.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/update_profile.php). User inputs are never directly concatenated into raw SQL strings, completely neutralizing SQL injection risks."* |
| **Q8: How scalable is the system if the farm expands from 3 coop zones to 10 coop buildings?** | *"The architecture was designed with modular scalability: (1) In [`api/config.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/api/config.php), the `ALLOWED_ZONES` array and `sensor_zones` database table can register any number of additional zones (`COOP4`, `COOP5`, etc.); (2) The Android app's `ZoneStatusGrid` dynamically renders cards based on the JSON map received from the backend, automatically adapting its layout; (3) On the hardware tier, additional ESP32 nodes can be deployed across buildings, each transmitting their unique `zone` identifier over Wi-Fi to the central API."* |
| **Q9: What is the purpose of having both an Activity Log and a Triggered Alerts screen?** | *"This separation adheres to the principle of **task-oriented UX design**: (1) **Triggered Alerts** ([`TriggeredAlertsScreen.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/TriggeredAlertsScreen.kt)) is an urgent incident management screen tailored for rapid response and pattern analysis, equipped with date and zone filtering; (2) **Activity Log** ([`ActivityLogScreen.kt`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/app/src/main/java/com/example/mbpsaas_mobile_based_poultry_security_and_alert_system/ui/ActivityLogScreen.kt)) is a forensic audit trail recording both when motion started and when it ceased, allowing administrators to verify sensor health, system uptime, and exact intrusion durations."* |
| **Q10: What power backup measures are in place if an intruder cuts electrical power to the farm coop?** | *"Because the microcontroller and PIR sensors operate at low DC voltage (5V and 3.3V DC), the hardware hub is designed to be paired with a 12V rechargeable lead-acid or lithium-ion battery buffer with a step-down buck converter (or an uninterruptible 5V USB power bank). Even if the main AC electrical line is severed, the microcontroller, sensors, siren, and SIM800L cellular module continue operating autonomously on battery reserves."* |

---

## 💡 Pro-Tips for Defense Day

1. **Mirror Your Device Seamlessly:**
   * Use `scrcpy` in full-screen or Android Studio's built-in Device Mirroring tool. This allows all panelists to clearly witness the live Android UI transitions as motion is triggered.
2. **Keep the Software Simulator Ready:**
   * Bookmark [`tools/simulate_motion.php`](file:///C:/Users/GLENN/AndroidStudioProjects/MBPSAASMobileBasedPoultrySecurityandAlertSystem/tools/simulate_motion.php) on your browser. If a loose jumper wire or USB port glitch occurs with the physical breadboard during the presentation, you can seamlessly trigger intrusions from the web simulator without pausing the demonstration.
3. **Pre-test the GSM SMS Recipient:**
   * Verify that the SIM card in the SIM800L module has active prepaid load and cellular signal (indicated by the netlight LED blinking once every 3 seconds). Have the recipient phone placed where its screen can be shown to the camera or panelists.
4. **Use ADB Reverse for Zero Network Lag:**
   * Avoid relying on unstable venue Wi-Fi during the defense. Keep the phone tethered via USB with `tools/adb_reverse.bat` active. This provides lightning-fast sub-50ms API response times.
5. **Demonstrate Cross-Disciplinary Competence:**
   * When presenting, emphasize how your team solved both **hardware challenges** (sensor debouncing, GSM UART timing, power filtering capacitors) and **software challenges** (coroutine polling loops, Jetpack Compose reactive state management, secure PHP REST APIs). Panelists love seeing comprehensive full-stack engineering!
