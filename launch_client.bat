@echo off
setlocal
cd /d "%~dp0"
call "%~dp0gradlew.bat" runClient --no-daemon
if errorlevel 1 (
    echo.
    echo Client exited with code %errorlevel%.
    pause
)
