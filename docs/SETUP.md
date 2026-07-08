# MBPSAAS — Setup on a New Laptop

Follow this from top to bottom on a fresh machine and you'll end up with the
exact same working setup.

## 1. Install the tools

| Tool | Notes |
|---|---|
| Git | git-scm.com |
| Android Studio | Latest stable. Let it install the Android SDK when it first opens. |
| XAMPP | apachefriends.org — PHP 8+, includes Apache + MySQL/MariaDB |

## 2. Clone the project

```powershell
cd C:\Users\<you>\AndroidStudioProjects
git clone https://github.com/Glenn-IT/MBPSAAS-Mobile-Based-Poultry-Security-and-Alert-System.git
```

## 3. Hook the PHP API into XAMPP

The API source code lives in the repo's `api/` folder. XAMPP needs to serve it
as `http://localhost/mbpsaas_api/`. Create a folder junction (a shortcut that
Apache follows) — run in PowerShell:

```powershell
New-Item -ItemType Junction -Path C:\xampp\htdocs\mbpsaas_api -Target "C:\Users\<you>\AndroidStudioProjects\MBPSAAS-Mobile-Based-Poultry-Security-and-Alert-System\api"
```

With the junction, any edit to `api/` files is instantly live in XAMPP **and**
tracked by git — there is only one copy of the code.
(Alternative if junctions give you trouble: plain-copy the `api` folder to
`C:\xampp\htdocs\` and rename it `mbpsaas_api` — but then you must copy it
back into the repo whenever you change the PHP.)

## 4. Create the database

1. Open **XAMPP Control Panel** → Start **Apache** and **MySQL**.
2. Open `http://localhost/mbpsaas_api/setup.php` in a browser.
   It creates database `mbpsaas_db`, the `users` table, and the admin account.

> The database itself is NOT in git — each laptop has its own MySQL data.
> A fresh laptop always starts with the default `admin` / `admin123` account,
> even if you changed the password on the other laptop.

## 5. Point the app at this laptop

1. Find this laptop's Wi-Fi IP: run `ipconfig` → "Wireless LAN adapter Wi-Fi"
   → IPv4 Address (e.g. `192.168.1.7`).
2. Edit `app/src/main/java/.../data/ApiClient.kt` and set:
   ```kotlin
   private const val BASE_URL = "http://<that-ip>/mbpsaas_api/"
   ```
   (For the Android emulator instead of a real phone, use `http://10.0.2.2/mbpsaas_api/`.)

## 6. Run the app

1. Open the project folder in Android Studio, let Gradle sync finish.
2. Phone: enable **USB debugging** (Settings → Developer options), plug in USB,
   and connect the phone to the **same Wi-Fi** as the laptop.
3. Press Run ▶.

Log in with `admin` / `admin123`.

## Troubleshooting

- **"Cannot reach server"** on the phone:
  - Apache running? Phone on the same Wi-Fi? `BASE_URL` IP correct?
  - Windows Firewall may block Apache — allow it (or test by browsing to
    `http://<laptop-ip>/mbpsaas_api/get_questions.php` from the phone's browser;
    if that fails, it's network/firewall, not the app).
- **"Database connection failed"**: MySQL not started, or `setup.php` never run.
- **Gradle can't find Java** (terminal builds only): set
  `JAVA_HOME` to Android Studio's bundled JDK, e.g.
  `C:\Program Files\Android\Android Studio\jbr`.
- Port 80 already taken (Skype/IIS): change Apache's port in XAMPP, then add
  the port to `BASE_URL`, e.g. `http://192.168.1.7:8080/mbpsaas_api/`.

## What's where

| Path | What |
|---|---|
| `app/` | Android app (Kotlin + Jetpack Compose) |
| `api/` | PHP API served by XAMPP via the junction |
| `docs/CHECKLIST.md` | Progress + roadmap + decisions |
| `docs/SETUP.md` | This file |
| `docs/img/` | Logo source image |
