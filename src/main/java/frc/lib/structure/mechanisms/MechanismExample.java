package frc.lib.structure.mechanisms;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.ejml.simple.SimpleMatrix;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.structure.configBase;
import frc.lib.structure.inputBase;
import frc.lib.structure.requestBase;
import frc.lib.structure.motors.KrakenIO;
import frc.lib.structure.motors.MotorConfig;
import frc.lib.structure.physics.physicalProperties;

/**
 * Example implementation of a mechanism system Demonstrates how to use the mechanism framework in a
 * real WPILib subsystem
 */
public class MechanismExample extends SubsystemBase {

    private MechanismSystem mechanismSystem;
    private Map<String, SetPoint> currentSetpoints;

    // Example mechanisms
    private LinearMechanism<configBase, inputBase, requestBase> elevator;
    private RotatingMechanism<configBase, inputBase, requestBase> arm;

    // Example motors
    private KrakenIO elevatorMotor;
    private KrakenIO armMotor;

    public MechanismExample() {
        // Initialize mechanism system
        mechanismSystem = new MechanismSystem("ExampleRobot");
        currentSetpoints = new HashMap<>();

        // Create physical properties
        SimpleMatrix elevatorCG = new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.5); // Center of mass at
                                                                               // 0.5m height
        SimpleMatrix elevatorMOI =
                new SimpleMatrix(3, 3, true, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.1); // Moment
                                                                                           // of
                                                                                           // inertia
                                                                                           // matrix
        physicalProperties elevatorProps =
                new physicalProperties(10.0, elevatorCG, elevatorMOI, Optional.empty());

        SimpleMatrix armCG = new SimpleMatrix(3, 1, true, 0.3, 0.0, 0.0); // Center of mass at 0.3m
                                                                          // from pivot
        SimpleMatrix armMOI =
                new SimpleMatrix(3, 3, true, 0.1, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0, 0.0, 0.5); // Moment
                                                                                           // of
                                                                                           // inertia
                                                                                           // matrix
        physicalProperties armProps = new physicalProperties(5.0, armCG, armMOI, Optional.empty());

        // Create mechanisms
        SimpleMatrix elevatorAxis = new SimpleMatrix(3, 1, true, 0.0, 0.0, 1.0); // Vertical motion
        SimpleMatrix elevatorStart = new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.0); // Start at ground
                                                                                  // level

        SimpleMatrix armAxis = new SimpleMatrix(3, 1, true, 0.0, 0.0, 1.0); // Rotation around Z
                                                                            // axis
        SimpleMatrix armPivot = new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.0); // Pivot at origin

        elevator = new LinearMechanism<>("Elevator", elevatorProps, elevatorAxis, elevatorStart);
        arm = new RotatingMechanism<>("Arm", armProps, armAxis, armPivot);

        // Create motor configurations
        MotorConfig elevatorConfig = new MotorConfig("Elevator", 1, "rio");
        elevatorConfig.kP = 0.1;
        elevatorConfig.kI = 0.0;
        elevatorConfig.kD = 0.01;
        elevatorConfig.maxVel = 5.0; // m/s
        elevatorConfig.maxAcc = 10.0; // m/s²
        elevatorConfig.gearRatio = 10.0; // Gear ratio
        elevatorConfig.reversed = false;
        elevatorConfig.isBreak = true;
        elevatorConfig.supplyCurrentLimitEnabled = true;
        elevatorConfig.supplyCurrentLimit = 40.0; // Amps
        elevatorConfig.statorCurrentLimitEnabled = true;
        elevatorConfig.statorCurrentLimit = 40.0; // Amps
        elevatorConfig.updateFrequency = 100; // Hz
        elevatorConfig.isInnerSyncronized = true;

        MotorConfig armConfig = new MotorConfig("Arm", 2, "rio");
        armConfig.kP = 0.2;
        armConfig.kI = 0.0;
        armConfig.kD = 0.02;
        armConfig.maxVel = 10.0; // rad/s
        armConfig.maxAcc = 20.0; // rad/s²
        armConfig.gearRatio = 20.0; // Gear ratio
        armConfig.reversed = false;
        armConfig.isBreak = true;
        armConfig.supplyCurrentLimitEnabled = true;
        armConfig.supplyCurrentLimit = 30.0; // Amps
        armConfig.statorCurrentLimitEnabled = true;
        armConfig.statorCurrentLimit = 30.0; // Amps
        armConfig.updateFrequency = 100; // Hz
        armConfig.isInnerSyncronized = true;

        // Create motors for elevator (dual motor setup)
        elevatorMotor = new KrakenIO(elevatorConfig);

        MotorConfig elevatorConfig2 = new MotorConfig("Elevator2", 3, "rio");
        elevatorConfig2.kP = 0.1;
        elevatorConfig2.kI = 0.0;
        elevatorConfig2.kD = 0.01;
        elevatorConfig2.maxVel = 5.0;
        elevatorConfig2.maxAcc = 10.0;
        elevatorConfig2.gearRatio = 10.0;
        elevatorConfig2.reversed = false;
        elevatorConfig2.isBreak = true;
        elevatorConfig2.supplyCurrentLimitEnabled = true;
        elevatorConfig2.supplyCurrentLimit = 40.0;
        elevatorConfig2.statorCurrentLimitEnabled = true;
        elevatorConfig2.statorCurrentLimit = 40.0;
        elevatorConfig2.updateFrequency = 100;
        elevatorConfig2.isInnerSyncronized = true;

        KrakenIO elevatorMotor2 = new KrakenIO(elevatorConfig2);

        // Create motors for arm (single motor setup)
        armMotor = new KrakenIO(armConfig);

        // Register motors with mechanisms
        elevator.registerMotor(elevatorMotor, elevatorConfig);
        elevator.registerMotor(elevatorMotor2, elevatorConfig2);
        arm.registerMotor(armMotor, armConfig);

        // Add mechanisms to system
        mechanismSystem.addMechanism(elevator);
        mechanismSystem.addMechanism(arm);

        // Set parent-child relationship (elevator is parent, arm is child)
        mechanismSystem.setParentChildRelation("Elevator", "Arm");

        // Set control parameters
        elevator.setControlParameters(0.01, 0.1); // 1cm position tolerance, 0.1 m/s velocity
                                                  // tolerance
        arm.setControlParameters(0.01, 0.1); // 0.01 rad position tolerance, 0.1 rad/s velocity
                                             // tolerance
    }

    @Override
    public void periodic() {
        // This method is called every 20ms by WPILib

        // Update all mechanism states from motor feedback
        mechanismSystem.updateAllMechanismStates();

        // Execute control for all mechanisms
        mechanismSystem.executeAllMechanismControl();
    }

    /**
     * Set elevator to a specific height
     */
    public void setElevatorHeight(double height, double velocity, double acceleration) {
        SetPoint setpoint = new SetPoint(height, velocity, acceleration, 0.0);
        elevator.setTargetSetpoint(setpoint);
        currentSetpoints.put("Elevator", setpoint);
    }

    /**
     * Set arm to a specific angle
     */
    public void setArmAngle(double angle, double angularVelocity, double angularAcceleration) {
        SetPoint setpoint = new SetPoint(angle, angularVelocity, angularAcceleration, 0.0);
        arm.setTargetSetpoint(setpoint);
        currentSetpoints.put("Arm", setpoint);
    }

    /**
     * Check if all mechanisms are at target
     */
    public boolean areAllMechanismsAtTarget() {
        return mechanismSystem.areAllMechanismsAtTarget();
    }

    /**
     * Emergency stop all mechanisms
     */
    public void emergencyStop() {
        mechanismSystem.emergencyStopAllMechanisms();
    }
}
