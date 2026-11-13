@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1
if errorlevel 1 (
  echo Java is missing from PATH.
  echo Install Java version 17 or newer, then reopen this file.
  pause
  exit /b 1
)
java -jar SkylineRunner.jar
if errorlevel 1 pause
