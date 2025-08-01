@echo off
echo Starting WPILib Simulation with Driver Station support...
echo.
echo This will start the simulation and enable connection to Driver Station
echo Make sure Driver Station is running and configured for simulation
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
echo Starting simulation...
echo.
echo Instructions:
echo 1. Wait for simulation to start
echo 2. Open Driver Station
echo 3. Set Driver Station to "Simulation" mode
echo 4. Enable the robot
echo 5. Press A button to test motor simulation
echo.
echo Press Ctrl+C to stop simulation
echo.

call gradlew.bat simulateJava

echo.
echo Simulation ended.
pause 