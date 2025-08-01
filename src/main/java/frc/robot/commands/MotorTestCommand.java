package frc.robot.commands;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.MotorTestSubsystem;

/**
 * Command to test motor simulation using motion profile
 * Generates smooth position and velocity trajectories
 */
public class MotorTestCommand extends Command {

    private final MotorTestSubsystem motorTestSubsystem;
    private double commandStartTime;

    // Motion profile parameters
    private static final double MAX_VELOCITY = 2.0; // rad/s
    private static final double MAX_ACCELERATION = 4.0; // rad/s²
    private static final double PROFILE_DURATION = 2.0; // seconds per segment

    // Test sequence waypoints
    private static final double[] WAYPOINTS = { 0.0, 1.0, -1.0, 0.0 }; // radians
    private int currentWaypoint = 0;

    // Motion profile
    private TrapezoidProfile currentProfile;
    private TrapezoidProfile.State startState;
    private TrapezoidProfile.State endState;
    private double profileStartTime;
    private boolean profileActive = false;

    public MotorTestCommand(MotorTestSubsystem motorTestSubsystem) {
        this.motorTestSubsystem = motorTestSubsystem;
        addRequirements(motorTestSubsystem);
    }

    @Override
    public void initialize() {
        commandStartTime = System.currentTimeMillis() / 1000.0;
        currentWaypoint = 0;
        profileActive = false;

        // Get current motor state
        var inputs = motorTestSubsystem.getMotorInputs();
        startState = new TrapezoidProfile.State(inputs.position, inputs.velocity);

        Logger.recordOutput("MotorTestCommand/Initialized", true);
        Logger.recordOutput("MotorTestCommand/StartTime", commandStartTime);
        Logger.recordOutput("MotorTestCommand/CurrentWaypoint", currentWaypoint);
    }

    @Override
    public void execute() {
        double elapsedTime = System.currentTimeMillis() / 1000.0 - commandStartTime;

        // Check if we need to start a new profile
        if (!profileActive && currentWaypoint < WAYPOINTS.length) {
            startNewProfile();
        }

        // Execute current profile
        if (profileActive) {
            executeProfile(elapsedTime);
        }

        // Log command state
        Logger.recordOutput("MotorTestCommand/ElapsedTime", elapsedTime);
        Logger.recordOutput("MotorTestCommand/CurrentWaypoint", currentWaypoint);
        Logger.recordOutput("MotorTestCommand/ProfileActive", profileActive);
        Logger.recordOutput("MotorTestCommand/TotalWaypoints", WAYPOINTS.length);

        // Log motor performance
        var inputs = motorTestSubsystem.getMotorInputs();
        Logger.recordOutput("MotorTestCommand/CurrentPosition", inputs.position);
        Logger.recordOutput("MotorTestCommand/CurrentVelocity", inputs.velocity);
        Logger.recordOutput("MotorTestCommand/Current", inputs.current);
        Logger.recordOutput("MotorTestCommand/Temperature", inputs.temperature);
    }

    private void startNewProfile() {
        // Get current motor state
        var inputs = motorTestSubsystem.getMotorInputs();
        startState = new TrapezoidProfile.State(inputs.position, inputs.velocity);

        // Set end state for current waypoint
        endState = new TrapezoidProfile.State(WAYPOINTS[currentWaypoint], 0.0);

        // Create trapezoid profile with constraints
        TrapezoidProfile.Constraints constraints = new TrapezoidProfile.Constraints(MAX_VELOCITY, MAX_ACCELERATION);
        currentProfile = new TrapezoidProfile(constraints);

        // Start profile execution
        profileStartTime = System.currentTimeMillis() / 1000.0;
        profileActive = true;

        Logger.recordOutput("MotorTestCommand/ProfileStarted", true);
        Logger.recordOutput("MotorTestCommand/TargetPosition", WAYPOINTS[currentWaypoint]);
        Logger.recordOutput("MotorTestCommand/StartPosition", startState.position);
        Logger.recordOutput("MotorTestCommand/StartVelocity", startState.velocity);
        Logger.recordOutput("MotorTestCommand/ProfileTotalTime", currentProfile.totalTime());
    }

    private void executeProfile(double elapsedTime) {
        double profileTime = System.currentTimeMillis() / 1000.0 - profileStartTime;

        // Calculate profile state
        TrapezoidProfile.State profileState = currentProfile.calculate(profileTime, startState, endState);

        // Check if profile is complete (use a small tolerance)
        double totalTime = currentProfile.totalTime();
        if (profileTime >= totalTime - 0.01) {
            // Profile complete, move to next waypoint
            currentWaypoint++;
            profileActive = false;

            Logger.recordOutput("MotorTestCommand/ProfileCompleted", true);
            Logger.recordOutput("MotorTestCommand/ProfileTime", profileTime);
            Logger.recordOutput("MotorTestCommand/TotalTime", totalTime);

            return;
        }

        // Calculate acceleration from velocity change (simplified)
        double acceleration = 0.0;
        if (profileTime > 0.02) { // Small time step to avoid division by zero
            // Use a simple finite difference for acceleration
            TrapezoidProfile.State prevState = currentProfile.calculate(profileTime - 0.02, startState, endState);
            acceleration = (profileState.velocity - prevState.velocity) / 0.02;
        }

        // Apply to motor using the existing setPosition method
        motorTestSubsystem.setPosition(profileState.position, profileState.velocity, acceleration);

        // Log profile data
        Logger.recordOutput("MotorTestCommand/ProfileTime", profileTime);
        Logger.recordOutput("MotorTestCommand/ProfilePosition", profileState.position);
        Logger.recordOutput("MotorTestCommand/ProfileVelocity", profileState.velocity);
        Logger.recordOutput("MotorTestCommand/ProfileAcceleration", acceleration);
        Logger.recordOutput("MotorTestCommand/TotalTime", totalTime);
    }

    @Override
    public void end(boolean interrupted) {
        motorTestSubsystem.stopTest();
        Logger.recordOutput("MotorTestCommand/Interrupted", true);

        double totalTime = System.currentTimeMillis() / 1000.0 - commandStartTime;
        Logger.recordOutput("MotorTestCommand/TotalTime", totalTime);
        Logger.recordOutput("MotorTestCommand/FinalWaypoint", currentWaypoint);
    }

    @Override
    public boolean isFinished() {
        // Command finishes when all waypoints are completed
        return currentWaypoint >= WAYPOINTS.length;
    }
}