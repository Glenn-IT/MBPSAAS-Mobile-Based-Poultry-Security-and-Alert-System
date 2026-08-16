@echo off
title MBPSAAS ADB USB Tunnel Setup
color 0A

echo ============================================================
echo  MBPSAAS ADB USB Tunnel Setup
echo ============================================================
echo.

set ADB_PATH=

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set "ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
) else if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    set "ADB_PATH=%ANDROID_HOME%\platform-tools\adb.exe"
) else if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" (
    set "ADB_PATH=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
) else (
    set "ADB_PATH=adb"
)

echo Using ADB: "%ADB_PATH%"
echo.

"%ADB_PATH%" reverse tcp:8080 tcp:80

if %ERRORLEVEL% EQU 0 (
    echo.
    echo  SUCCESS: USB ADB Tunnel active! (8080 -^> 80)
    echo  Your phone can now access http://localhost:8080/mbpsaas_api/
    echo.
) else (
    echo.
    echo  ERROR: Could not establish ADB tunnel.
    echo  Make sure:
    echo    1. USB Cable is plugged in.
    echo    2. USB Debugging is ON in developer settings on your phone.
    echo    3. Tap "Allow USB Debugging" prompt on your phone screen if prompted.
    echo.
)
pause
