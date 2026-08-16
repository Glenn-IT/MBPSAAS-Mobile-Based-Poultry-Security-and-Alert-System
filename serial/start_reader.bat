@echo off
title MBPSAAS Serial Reader Bridge (Auto-Detect COM Port)
color 0B

cd /d "%~dp0"

set PORT=%1
if "%PORT%"=="" set PORT=AUTO

echo ============================================================
echo  Starting MBPSAAS Serial Reader Bridge...
echo  Target Port: %PORT% (Auto-Resolving if not specified)
echo ============================================================
echo.
echo  !! IMPORTANT !!
echo  Make sure the Arduino IDE Serial Monitor is CLOSED!
echo  Only one application can access the COM port at a time.
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0serial_reader.ps1" -ComPort %PORT%

echo.
echo ============================================================
echo  The serial reader bridge has stopped.
echo ============================================================
pause
