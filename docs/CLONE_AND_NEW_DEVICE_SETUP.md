# MBPSAAS — Cloning & Setup Guide for a New Device

This guide provides step-by-step instructions for cloning and setting up the **MBPSAAS (Mobile-Based Poultry Security and Alert System)** on a new PC/laptop, including how to preserve or restore existing database records from an old device.

---

## 1. Prerequisites (Software Installation)

Install the following software on the new device:
1. **Git**: [git-scm.com](https://git-scm.com/)
2. **Android Studio**: [developer.android.com/studio](https://developer.android.com/studio) (Includes Android SDK & platform-tools)
3. **XAMPP** (Apache + MySQL + PHP 8+): [apachefriends.org](https://www.apachefriends.org/) (Installed at `C:\xampp`)
4. **Arduino IDE**: [arduino.cc](https://www.arduino.cc/en/software)

---

## 2. Step 1 — Clone the Repository

Open PowerShell or Command Prompt on your new PC:

```powershell
cd C:\Users\<YourUsername>\AndroidStudioProjects
git clone https://github.com/Glenn-IT/MBPSAAS-Mobile-Based-Poultry-Security-and-Alert-System.git
cd MBPSAAS-Mobile-Based-Poultry-Security-and-Alert-System
```

---

## 3. Step 2 — Link PHP API into XAMPP (`mbpsaas_api`)

XAMPP serves web applications from `C:\xampp\htdocs`. Create a Windows Folder Junction (a live shortcut) so Apache reads `api/` directly from your cloned repo:

Open **PowerShell as Administrator**:

```powershell
New-Item -ItemType Junction -Path "C:\xampp\htdocs\mbpsaas_api" -Target "C:\Users\<YourUsername>\AndroidStudioProjects\MBPSAAS-Mobile-Based-Poultry-Security-and-Alert-System\api"
```

> [!NOTE]
> Replace `<YourUsername>` with your actual Windows user folder name. Now, any file inside `api/` is instantly live in XAMPP and tracked by Git!

---

## 4. Step 3 — Database Setup & Transferring Old Data

### Option A: You Have Existing Data from an Old Device (Import Database)
1. Open XAMPP Control Panel and start **Apache** and **MySQL**.
2. Open phpMyAdmin at `http://localhost/phpmyadmin`.
3. Click **Import** at the top menu.
4. Select your `.sql` backup file from your old device (or `database/database.sql`).
5. Click **Import** to restore all your existing motion logs, SMS alerts, and accounts!
6. Open `http://localhost/mbpsaas_api/setup.php` in your browser to verify table structures (`users`, `sensor_zones`, `motion_logs`, `sms_logs`).

### Option B: Fresh Database Setup (New Machine)
1. Start **Apache** and **MySQL** in XAMPP Control Panel.
2. Open your browser to:
   ```text
   http://localhost/mbpsaas_api/setup.php
   ```
3. This creates database `motion_monitoring` with all tables (`motion_logs`, `sms_logs`, `users`, `sensor_zones`) and seeds the default admin account:
   - **Username**: `admin`
   - **Password**: `admin123`

---

## 5. Step 4 — Connect Physical Android Phone (USB Cable)

1. Enable **USB Debugging** on your phone (Settings -> Developer Options -> USB Debugging).
2. Plug your phone into the PC via USB cable.
3. Open project folder and double-click:
   ```text
   tools/adb_reverse.bat
   ```
   *(Or run: `adb reverse tcp:8080 tcp:80`)*
4. Confirm terminal logs `SUCCESS: USB ADB Tunnel active!`.

---

## 6. Step 5 — Flash Arduino & Start Serial Reader Bridge

1. Connect Arduino Uno to USB port on the new PC.
2. Open Device Manager -> Ports (COM & LPT) to check your COM port (e.g. `COM3` or `COM5`).
3. Open `arduino/poultry_sensor/poultry_sensor.ino` in Arduino IDE:
   - Select Board: **Arduino Uno**
   - Select Port: **Your COM Port**
   - Upload the sketch.
   - **IMPORTANT**: Close the Arduino IDE Serial Monitor window.
4. Launch the serial bridge listener in PowerShell:
   ```cmd
   cd C:\Users\<YourUsername>\AndroidStudioProjects\MBPSAAS-Mobile-Based-Poultry-Security-and-Alert-System\serial
   .\start_reader.bat COMx
   ```
   *(Replace `COMx` with your actual port, e.g. `.\start_reader.bat COM5`)*

---

## 7. Step 6 — Run the Android Application

1. Open Android Studio and select **File > Open**, then choose the cloned project folder:
   `C:\Users\<YourUsername>\AndroidStudioProjects\MBPSAAS-Mobile-Based-Poultry-SecurityandAlertSystem`
2. Wait for Gradle sync to complete.
3. Select your physical phone in the device dropdown.
4. Click **Run ▶**.
5. Log in using `admin` / `admin123`.

---

## 8. Verification & Quick Diagnostic Checklist

| Test Item | URL / Command | Expected Result |
|---|---|---|
| API Health Check | `http://localhost/mbpsaas_api/get_motion_events.php` | Returns JSON with `"success": true` and zone map. |
| Web Simulator | `http://localhost/mbpsaas_api/tools/simulate_motion.php` | Lets you click buttons to test motion triggers. |
| USB Tunnel | `tools/adb_reverse.bat` | Displays `SUCCESS: USB ADB Tunnel active`. |
| Serial Bridge | `serial/start_reader.bat COMx` | Displays `[READY] Listening for serial events on COMx`. |
