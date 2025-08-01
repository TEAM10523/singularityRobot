package frc.robot.commands;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.RotatingMechanismTestSubsystem;

public class RotatingMechanismTestCommand extends Command {
    private final RotatingMechanismTestSubsystem rotatingMechanismSubsystem;
    private double commandStartTime;

    // Motion profile constraints
    private static final double MAX_ANGULAR_VELOCITY = 2.0; // rad/s
    private static final double MAX_ANGULAR_ACCELERATION = 4.0; // rad/s²

    // Test waypoints (angles in radians)
    private static final double[] WAYPOINTS = { 0.0, Math.PI / 4, -Math.PI / 4, Math.PI / 2, 0.0 }; // 0°, 45°, -45°,
                                                                                                    // 90°, 0°

    private int currentWaypoint = 0;
    private TrapezoidProfile currentProfile;
    private TrapezoidProfile.State startState;
    private TrapezoidProfile.State endState;
    private double profileStartTime;
    private boolean profileActive = false;

    public RotatingMechanismTestCommand(RotatingMechanismTestSubsystem rotatingMechanismSubsystem) {
        this.rotatingMechanismSubsystem = rotatingMechanismSubsystem;
        addRequirements(rotatingMechanismSubsystem);
    }

    @Override
    public void initialize() {
        commandStartTime = System.currentTimeMillis() / 1000.0;
        currentWaypoint = 0;
        profileActive = false;

        Logger.recordOutput("RotatingMechanismTestCommand/Initialized", true);
        Logger.recordOutput("RotatingMechanismTestCommand/TotalWaypoints", WAYPOINTS.length);
        Logger.recordOutput("RotatingMechanismTestCommand/MaxAngularVelocity", MAX_ANGULAR_VELOCITY);
        Logger.recordOutput("RotatingMechanismTestCommand/MaxAngularAcceleration", MAX_ANGULAR_ACCELERATION);
    }

    @Override
    public void execute() {
        double elapsedTime = System.currentTimeMillis() / 1000.0 - commandStartTime;

        // Start new profile if needed
        if (!profileActive && currentWaypoint < WAYPOINTS.length) {
            startNewProfile();
        }

        // Execute current profile
        if (profileActive) {
            executeProfile(elapsedTime);
        }

        // Log command state
        Logger.recordOutput("RotatingMechanismTestCommand/ElapsedTime", elapsedTime);
        Logger.recordOutput("RotatingMechanismTestCommand/CurrentWaypoint", currentWaypoint);
        Logger.recordOutput("RotatingMechanismTestCommand/ProfileActive", profileActive);
        Logger.recordOutput("RotatingMechanismTestCommand/TotalWaypoints", WAYPOINTS.length);

        // Log mechanism performance
        Logger.recordOutput("RotatingMechanismTestCommand/CurrentAngle", rotatingMechanismSubsystem.getCurrentAngle());
        Logger.recordOutput("RotatingMechanismTestCommand/CurrentAngularVelocity",
                rotatingMechanismSubsystem.getAngularVelocity());
        Logger.recordOutput("RotatingMechanismTestCommand/AtTarget", rotatingMechanismSubsystem.isAtTarget());
    }

    private void startNewProfile() {
        // Get current mechanism state
        startState = new TrapezoidProfile.State(rotatingMechanismSubsystem.getCurrentAngle(),
                rotatingMechanismSubsystem.getAngularVelocity());

        // Set end state for current waypoint
        endState = new TrapezoidProfile.State(WAYPOINTS[currentWaypoint], 0.0);

        // Create trapezoid profile with constraints
        TrapezoidProfile.Constraints constraints = new TrapezoidProfile.Constraints(MAX_ANGULAR_VELOCITY,
                MAX_ANGULAR_ACCELERATION);
        currentProfile = new TrapezoidProfile(constraints);

        // Start profile execution
        profileStartTime = System.currentTimeMillis() / 1000.0;
        profileActive = true;

        Logger.recordOutput("RotatingMechanismTestCommand/ProfileStarted", true);
        Logger.recordOutput("RotatingMechanismTestCommand/TargetAngle", WAYPOINTS[currentWaypoint]);
        Logger.recordOutput("RotatingMechanismTestCommand/StartAngle", startState.position);
        Logger.recordOutput("RotatingMechanismTestCommand/StartAngularVelocity", startState.velocity);
        Logger.recordOutput("RotatingMechanismTestCommand/ProfileTotalTime", currentProfile.totalTime());
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

            Logger.recordOutput("RotatingMechanismTestCommand/ProfileCompleted", true);
            Logger.recordOutput("RotatingMechanismTestCommand/ProfileTime", profileTime);
            Logger.recordOutput("RotatingMechanismTestCommand/TotalTime", totalTime);

            return;
        }

        // Calculate angular acceleration from velocity change (simplified)
        double angularAcceleration = 0.0;
        if (profileTime > 0.02) {
            // Use a simple finite difference for acceleration
            TrapezoidProfile.State prevState = currentProfile.calculate(profileTime - 0.02, startState, endState);
            angularAcceleration = (profileState.velocity - prevState.velocity) / 0.02;
        }

        // Apply to mechanism
        rotatingMechanismSubsystem.setTargetAngle(profileState.position, profileState.velocity, angularAcceleration);

        // Log profile data
        Logger.recordOutput("RotatingMechanismTestCommand/ProfileTime", profileTime);
        Logger.recordOutput("RotatingMechanismTestCommand/ProfileAngle", profileState.position);
        Logger.recordOutput("RotatingMechanismTestCommand/ProfileAngularVelocity", profileState.velocity);
        Logger.recordOutput("RotatingMechanismTestCommand/ProfileAngularAcceleration", angularAcceleration);
        Logger.recordOutput("RotatingMechanismTestCommand/TotalTime", totalTime);
    }

    @Override
    public void end(boolean interrupted) {
        rotatingMechanismSubsystem.stop();
        Logger.recordOutput("RotatingMechanismTestCommand/Interrupted", true);

        double totalTime = System.currentTimeMillis() / 1000.0 - commandStartTime;
        Logger.recordOutput("RotatingMechanismTestCommand/TotalTime", totalTime);
        Logger.recordOutput("RotatingMechanismTestCommand/FinalWaypoint", currentWaypoint);
    }

    @Override
    public boolean isFinished() {
        // Command finishes when all waypoints are completed
        return currentWaypoint >= WAYPOINTS.length;
    }
}