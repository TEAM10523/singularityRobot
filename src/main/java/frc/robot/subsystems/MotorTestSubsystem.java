package frc.robot.subsystems;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.structure.motors.KrakenSimIO;
import frc.lib.structure.motors.MotorConfig;
import frc.lib.structure.motors.MotorInputs;
import frc.lib.structure.motors.MotorRequest;

/**
 * Test subsystem for motor simulation and logging
 * Demonstrates how to use KrakenSimIO with AdvantageKit logging
 */
public class MotorTestSubsystem extends SubsystemBase {

    private KrakenSimIO motor;
    private MotorConfig config;
    private MotorInputs inputs = new MotorInputs();

    // Test parameters
    private double targetPosition = 0.0;
    private double targetVelocity = 0.0;
    private double targetAcceleration = 0.0;
    private double feedforward = 0.0;

    // Test state
    private boolean isTestRunning = false;
    private double testStartTime = 0.0;
    private int testStep = 0;

    public MotorTestSubsystem() {
        // Create motor configuration
        config = new MotorConfig("TestMotor", 1, "rio");
        config.kP = 1;
        config.kI = 0.0;
        config.kD = 1;
        config.maxVel = 10.0; // rad/s
        config.maxAcc = 20.0; // rad/s²
        config.gearRatio = 10.0;
        config.reversed = false;
        config.isBreak = true;
        config.supplyCurrentLimitEnabled = true;
        config.supplyCurrentLimit = 40.0;
        config.statorCurrentLimitEnabled = true;
        config.statorCurrentLimit = 40.0;
        config.updateFrequency = 100;
        config.isInnerSyncronized = true;

        // Create simulated motor
        motor = new KrakenSimIO(config);

        // Log subsystem initialization
        Logger.recordOutput("MotorTest/Initialized", true);
        Logger.recordOutput("MotorTest/Config", config.toString());
    }

    @Override
    public void periodic() {
        // Update motor inputs
        motor.updateInputs(inputs);

        // Log subsystem state
        logSubsystemState();

        // Run test sequence if active
        if (isTestRunning) {
            runTestSequence();
        }
    }

    /**
     * Start motor test sequence
     */
    public void startTest() {
        isTestRunning = true;
        testStartTime = System.currentTimeMillis() / 1000.0;
        testStep = 0;
        motor.reset();

        Logger.recordOutput("MotorTest/TestStarted", true);
        Logger.recordOutput("MotorTest/TestStartTime", testStartTime);
    }

    /**
     * Stop motor test sequence
     */
    public void stopTest() {
        isTestRunning = false;

        // Stop motor
        MotorRequest stopRequest = new MotorRequest()
                .withPosition(0.0)
                .withVelocity(0.0)
                .withAcceleration(0.0)
                .withFeedforward(0.0);
        motor.set(stopRequest);

        Logger.recordOutput("MotorTest/TestStopped", true);
    }

    /**
     * Set motor to specific position
     */
    public void setPosition(double position, double velocity, double acceleration) {
        targetPosition = position;
        targetVelocity = velocity;
        targetAcceleration = acceleration;

        MotorRequest request = new MotorRequest()
                .withPosition(position)
                .withVelocity(velocity)
                .withAcceleration(acceleration)
                .withFeedforward(0.0);
        motor.set(request);

        Logger.recordOutput("MotorTest/ManualSetpoint", position);
    }

    /**
     * Run automated test sequence
     */
    private void runTestSequence() {
        double currentTime = System.currentTimeMillis() / 1000.0 - testStartTime;

        // Test sequence: 0-2s: go to 1 rad, 2-4s: go to -1 rad, 4-6s: go to 0 rad
        if (currentTime < 2.0) {
            // Step 1: Move to 1 radian
            if (testStep != 1) {
                testStep = 1;
                MotorRequest request = new MotorRequest()
                        .withPosition(1.0)
                        .withVelocity(0.5)
                        .withAcceleration(0)
                        .withFeedforward(0.0);
                motor.set(request);

                Logger.recordOutput("MotorTest/TestStep", 1);
                Logger.recordOutput("MotorTest/StepTarget", 1.0);
            }
        } else if (currentTime < 4.0) {
            // Step 2: Move to -1 radian
            if (testStep != 2) {
                testStep = 2;
                MotorRequest request = new MotorRequest()
                        .withPosition(-1.0)
                        .withVelocity(0.5)
                        .withAcceleration(0)
                        .withFeedforward(0.0);
                motor.set(request);

                Logger.recordOutput("MotorTest/TestStep", 2);
                Logger.recordOutput("MotorTest/StepTarget", -1.0);
            }
        } else if (currentTime < 6.0) {
            // Step 3: Return to 0 radian
            if (testStep != 3) {
                testStep = 3;
                MotorRequest request = new MotorRequest()
                        .withPosition(0.0)
                        .withVelocity(0.5)
                        .withAcceleration(0)
                        .withFeedforward(0.0);
                motor.set(request);

                Logger.recordOutput("MotorTest/TestStep", 3);
                Logger.recordOutput("MotorTest/StepTarget", 0.0);
            }
        } else {
            // Test complete
            stopTest();
            Logger.recordOutput("MotorTest/TestCompleted", true);
        }
    }

    /**
     * Log subsystem state
     */
    private void logSubsystemState() {
        // Log test state
        Logger.recordOutput("MotorTest/IsTestRunning", isTestRunning);
        Logger.recordOutput("MotorTest/TestStep", testStep);
        Logger.recordOutput("MotorTest/TestTime", System.currentTimeMillis() / 1000.0 - testStartTime);

        // Log motor state
        Logger.recordOutput("MotorTest/CurrentPosition", inputs.position);
        Logger.recordOutput("MotorTest/CurrentVelocity", inputs.velocity);
        Logger.recordOutput("MotorTest/CurrentAcceleration", inputs.acceleration);
        Logger.recordOutput("MotorTest/CurrentCurrent", inputs.current);
        Logger.recordOutput("MotorTest/CurrentTemperature", inputs.temperature);
        Logger.recordOutput("MotorTest/Connected", inputs.connected);

        // Log target values
        Logger.recordOutput("MotorTest/TargetPosition", targetPosition);
        Logger.recordOutput("MotorTest/TargetVelocity", targetVelocity);
        Logger.recordOutput("MotorTest/TargetAcceleration", targetAcceleration);
        Logger.recordOutput("MotorTest/Feedforward", feedforward);

        // Log performance metrics
        double positionError = Math.abs(targetPosition - inputs.position);
        double velocityError = Math.abs(targetVelocity - inputs.velocity);
        Logger.recordOutput("MotorTest/PositionError", positionError);
        Logger.recordOutput("MotorTest/VelocityError", velocityError);
        Logger.recordOutput("MotorTest/AtTarget", positionError < 0.01 && velocityError < 0.1);
    }

    /**
     * Get motor inputs
     */
    public MotorInputs getMotorInputs() {
        return inputs;
    }

    /**
     * Check if test is running
     */
    public boolean isTestRunning() {
        return isTestRunning;
    }

    /**
     * Get current test step
     */
    public int getTestStep() {
        return testStep;
    }
}