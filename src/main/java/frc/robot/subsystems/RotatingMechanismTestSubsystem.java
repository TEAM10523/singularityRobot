package frc.robot.subsystems;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.structure.mechanisms.RotatingMechanism;
import frc.lib.structure.mechanisms.SetPoint;
import frc.lib.structure.motors.KrakenSimIO;
import frc.lib.structure.motors.MotorConfig;
import frc.lib.structure.motors.MotorInputs;
import frc.lib.structure.motors.MotorRequest;
import frc.lib.structure.physics.physicalProperties;

public class RotatingMechanismTestSubsystem extends SubsystemBase {
    private final KrakenSimIO motorIO;
    private final RotatingMechanism rotatingMechanism;
    private final MotorConfig config;
    private final MotorRequest motorRequest;

    // Physical properties for the rotating mechanism (e.g., an arm)
    private static final double ARM_LENGTH = 0.5; // meters
    private static final double ARM_MASS = 2.0; // kg
    private static final double MOMENT_OF_INERTIA = ARM_MASS * ARM_LENGTH * ARM_LENGTH / 3.0; // kg⋅m²
    private static final double GEAR_RATIO = 20.0; // 20:1 gear reduction
    private static final double EFFICIENCY = 0.85; // 85% efficiency

    public RotatingMechanismTestSubsystem() {
        // Create motor configuration
        config = new MotorConfig("ArmTestMotor", 2, "rio");
        config.kP = 5;
        config.kI = 0.0;
        config.kD = 2;
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

        // Create motor IO and inputs/outputs
        motorIO = new KrakenSimIO(config);
        motorRequest = new MotorRequest();

        // Create physical properties for the rotating mechanism
        // Create SimpleMatrix for CG and MOI
        org.ejml.simple.SimpleMatrix cgMatrix = new org.ejml.simple.SimpleMatrix(3, 1);
        cgMatrix.set(0, 0, ARM_LENGTH / 2.0); // CG at middle of arm
        cgMatrix.set(1, 0, 0.0);
        cgMatrix.set(2, 0, 0.0);

        org.ejml.simple.SimpleMatrix moiMatrix = new org.ejml.simple.SimpleMatrix(3, 3);
        moiMatrix.set(0, 0, MOMENT_OF_INERTIA); // Only about Z-axis
        moiMatrix.set(1, 1, 0.0);
        moiMatrix.set(2, 2, 0.0);

        physicalProperties physics = new physicalProperties(ARM_MASS, cgMatrix, moiMatrix, java.util.Optional.empty());

        // Create rotation axis and pivot point
        org.ejml.simple.SimpleMatrix rotationAxis = new org.ejml.simple.SimpleMatrix(3, 1);
        rotationAxis.set(0, 0, 0.0);
        rotationAxis.set(1, 0, 0.0);
        rotationAxis.set(2, 0, 1.0); // Rotate around Z-axis

        org.ejml.simple.SimpleMatrix pivotPoint = new org.ejml.simple.SimpleMatrix(3, 1);
        pivotPoint.set(0, 0, 0.0);
        pivotPoint.set(1, 0, 0.0);
        pivotPoint.set(2, 0, 0.0);

        // Create rotating mechanism
        rotatingMechanism = new RotatingMechanism("TestArm", physics, rotationAxis, pivotPoint);
        rotatingMechanism.registerMotor(motorIO, config);

        // Log initial setup
        Logger.recordOutput("RotatingMechanismTest/Setup/ArmLength", ARM_LENGTH);
        Logger.recordOutput("RotatingMechanismTest/Setup/ArmMass", ARM_MASS);
        Logger.recordOutput("RotatingMechanismTest/Setup/MomentOfInertia", MOMENT_OF_INERTIA);
        Logger.recordOutput("RotatingMechanismTest/Setup/GearRatio", GEAR_RATIO);
        Logger.recordOutput("RotatingMechanismTest/Setup/Efficiency", EFFICIENCY);
    }

    @Override
    public void periodic() {
        // Update mechanism state from motor inputs
        rotatingMechanism.updateMechanismState();

        // Execute control if setpoint is active
        rotatingMechanism.executeControl();

        // Log mechanism state
        Logger.recordOutput("RotatingMechanismTest/Mechanism/CurrentAngle", rotatingMechanism.getCurrentAngle());
        Logger.recordOutput("RotatingMechanismTest/Mechanism/AngularVelocity", rotatingMechanism.getAngularVelocity());
        Logger.recordOutput("RotatingMechanismTest/Mechanism/AngularAcceleration",
                rotatingMechanism.getAngularAcceleration());
        Logger.recordOutput("RotatingMechanismTest/Mechanism/AtTarget", rotatingMechanism.isAtTarget());

        // Log motor state from mechanism's motor inputs
        if (rotatingMechanism.getMotorCount() > 0) {
            var motorInputs = rotatingMechanism.getMotorInputs(0);
            Logger.recordOutput("RotatingMechanismTest/Motor/Position", motorInputs.position);
            Logger.recordOutput("RotatingMechanismTest/Motor/Velocity", motorInputs.velocity);
            Logger.recordOutput("RotatingMechanismTest/Motor/Current", motorInputs.current);
            Logger.recordOutput("RotatingMechanismTest/Motor/Temperature", motorInputs.temperature);
            Logger.recordOutput("RotatingMechanismTest/Motor/Connected", motorInputs.connected);
        }
    }

    /**
     * Set the target angle for the rotating mechanism
     * 
     * @param targetAngle        Target angle in radians
     * @param targetVelocity     Target angular velocity in rad/s
     * @param targetAcceleration Target angular acceleration in rad/s²
     */
    public void setTargetAngle(double targetAngle, double targetVelocity, double targetAcceleration) {
        SetPoint setpoint = new SetPoint(targetAngle, targetVelocity, targetAcceleration, 0.0);
        rotatingMechanism.setTargetSetpoint(setpoint);

        Logger.recordOutput("RotatingMechanismTest/Setpoint/Position", targetAngle);
        Logger.recordOutput("RotatingMechanismTest/Setpoint/Velocity", targetVelocity);
        Logger.recordOutput("RotatingMechanismTest/Setpoint/Acceleration", targetAcceleration);
    }

    /**
     * Get the current angle of the mechanism
     * 
     * @return Current angle in radians
     */
    public double getCurrentAngle() {
        return rotatingMechanism.getCurrentAngle();
    }

    /**
     * Get the current angular velocity of the mechanism
     * 
     * @return Current angular velocity in rad/s
     */
    public double getAngularVelocity() {
        return rotatingMechanism.getAngularVelocity();
    }

    /**
     * Check if the mechanism is at its target
     * 
     * @return true if at target, false otherwise
     */
    public boolean isAtTarget() {
        return rotatingMechanism.isAtTarget();
    }

    /**
     * Stop the mechanism
     */
    public void stop() {
        rotatingMechanism.emergencyStop();
        Logger.recordOutput("RotatingMechanismTest/Stopped", true);
    }

    /**
     * Get motor inputs for debugging
     * 
     * @return Motor inputs
     */
    public MotorInputs getMotorInputs() {
        if (rotatingMechanism.getMotorCount() > 0) {
            return rotatingMechanism.getMotorInputs(0);
        }
        return null;
    }
}