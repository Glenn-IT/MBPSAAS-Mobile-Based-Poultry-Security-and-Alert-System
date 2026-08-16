@echo off
echo ============================================================
echo  MBPSAAS ADB USB Tunnel Setup
echo ============================================================
echo.
"C:\Users\GLENN\AppData\Local\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:80

if %ERRORLEVEL% EQU 0 (
    echo.
    echo  SUCCESS: USB ADB Tunnel active!
    echo  Your phone can now access http://localhost:8080/mbpsaas_api/
    echo.
) else (
    echo.
    echo  ERROR: Could not establish ADB tunnel.
    echo  Make sure:
    echo    1. USB Cable is plugged in.
    echo    2. USB Debugging is ON in developer settings on your phone.
    echo.
)
pause
