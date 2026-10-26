@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-food-ordering.ps1"
if errorlevel 1 pause
