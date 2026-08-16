@echo off
title MBPSAAS Serial Reader Bridge
color 0B

cd /d "%~dp0"

set PORT=%1
if "%PORT%"=="" set PORT=COM5

echo ============================================================
echo  Starting MBPSAAS Serial Reader Bridge on %PORT%...
echo ============================================================
echo.
echo  !! IMPORTANT !!
echo  Make sure the Arduino IDE Serial Monitor is CLOSED!
echo  Only one application can access %PORT% at a time.
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0serial_reader.ps1" -ComPort %PORT%

echo.
echo ============================================================
echo  The serial reader bridge has stopped.
echo ============================================================
pause
