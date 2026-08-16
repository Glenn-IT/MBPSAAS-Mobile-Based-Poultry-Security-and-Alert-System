@echo off
echo Searching for active serial COM ports...
powershell -Command "Get-CimInstance -Class Win32_SerialPort | Select-Object DeviceID, Name, Description"
pause
