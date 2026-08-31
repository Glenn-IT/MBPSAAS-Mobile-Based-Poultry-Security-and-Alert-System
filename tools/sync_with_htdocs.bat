@echo off
title MBPSAAS - Sync with htdocs/ABMDMS
color 0B

echo ============================================================
echo  MBPSAAS ^<---^> ABMDMS Dual-Project Sync Utility
echo ============================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync_with_htdocs.ps1" -CheckOnly

echo.
echo Options:
echo   [1] Pull/Sync updates from htdocs\ABMDMS to Android Studio Project
echo   [2] Re-check differences only
echo   [3] Exit
echo.
set /p opt="Select an option (1-3): "

if "%opt%"=="1" (
    echo.
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync_with_htdocs.ps1" -SyncFromAbmdms
) else if "%opt%"=="2" (
    echo.
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync_with_htdocs.ps1" -CheckOnly
)

echo.
pause
