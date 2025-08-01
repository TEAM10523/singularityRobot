@echo off
echo ========================================
echo WPILib Simulation with Real Driver Station
echo ========================================
echo.
echo This script will start the robot simulation
echo WITHOUT the simulator GUI, using only the
echo real Driver Station for control and monitoring.
echo.
echo Prerequisites:
echo 1. Driver Station must be running
echo 2. Driver Station must be set to "Simulation" mode
echo 3. Xbox controller should be connected
echo.
echo Press any key to continue...
pause >nul

echo.
echo Building project...
call gradlew.bat build
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting simulation WITHOUT GUI...
echo.
echo IMPORTANT: Make sure Driver Station is configured:
echo 1. Open Driver Station
echo 2. Go to Setup tab
echo 3. Set Robot Mode to "Simulation"
echo 4. Connect Xbox controller (port 0)
echo 5. Click Enable when ready
echo.
echo The simulation will run in the background.
echo Use Driver Station to control and monitor the robot.
echo.
echo Press Ctrl+C to stop simulation
echo.

REM Set environment variable to disable GUI
set HALSIM_EXTENSIONS=

REM Start simulation without GUI
call gradlew.bat simulateJava

echo.
echo Simulation ended.
pause 