// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.structure.mechanisms;

import java.util.ArrayList;
import java.util.List;
import org.ejml.simple.SimpleMatrix;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.structure.actuator;
import frc.lib.structure.configBase;
import frc.lib.structure.inputBase;
import frc.lib.structure.requestBase;
import frc.lib.structure.sensor;
import frc.lib.structure.motors.MotorConfig;
import frc.lib.structure.motors.MotorIO;
import frc.lib.structure.motors.MotorInputs;
import frc.lib.structure.motors.MotorRequest;
import frc.lib.structure.physics.physicalObject;
import frc.lib.structure.physics.physicalProperties;

/** Add your docs here. */
public abstract class Mechanism<Tconfig extends configBase, Tinputs extends inputBase, Trequest extends requestBase>
        extends SubsystemBase
        implements physicalObject<Tconfig>, sensor<Tinputs>, actuator<Trequest> {

    private String name;
    private ArrayList<physicalObject<configBase>> attachingObjects = new ArrayList<>();
    private physicalProperties properties;

    // Motor control - support multiple motors
    protected List<MotorIO> motorIOs = new ArrayList<>();
    protected List<MotorConfig> motorConfigs = new ArrayList<>();
    protected List<MotorInputs> motorInputs = new ArrayList<>();
    protected SetPoint currentSetpoint;

    // Control parameters
    protected double positionTolerance = 0.01; // meters or radians
    protected double velocityTolerance = 0.1; // m/s or rad/s



    public Mechanism(String name, physicalProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    public void addPhysicalObjects(ArrayList<physicalObject<configBase>> objects) {
        attachingObjects.addAll(objects);
    }

    public void addPhysicalObject(physicalObject<configBase> object) {
        attachingObjects.add(object);
    }

    public void removePhysicalObject(physicalObject<configBase> object) {
        attachingObjects.remove(object);
    }

    public ArrayList<physicalObject<configBase>> getAttachingObjects() {
        return attachingObjects;
    }



    @Override
    public void setConfig(Tconfig config) {

    }

    @Override
    public String getDescription() {
        return "" + name; // TODO
    }

    /**
     * Get mechanism name
     * 
     * @return Mechanism name
     */
    public String getName() {
        return name;
    }

    @Override
    public physicalProperties getPhysicalProperties() {
        return this.properties;
    }

    @Override
    public SimpleMatrix getFeedforward(SimpleMatrix noninertialFrame) {
        return new SimpleMatrix(new double[] {0, 0, 0});
    }

    /**
     * Register motor for this mechanism
     * 
     * @param motorIO Motor interface
     * @param config Motor configuration
     */
    public void registerMotor(MotorIO motorIO, MotorConfig config) {
        this.motorIOs.add(motorIO);
        this.motorConfigs.add(config);
        this.motorInputs.add(new MotorInputs());
        motorIO.setConfig(config);
    }

    /**
     * Update mechanism state from motor feedback This should be called periodically (e.g., every
     * 20ms)
     */
    public void updateMechanismState() {
        if (!motorIOs.isEmpty()) {
            // Update all motor inputs
            for (int i = 0; i < motorIOs.size(); i++) {
                motorIOs.get(i).updateInputs(motorInputs.get(i));
            }
            updateStateFromMotorInputs();
        }
    }

    /**
     * Update mechanism state based on motor inputs Override in subclasses to handle specific
     * mechanism types
     */
    protected void updateStateFromMotorInputs() {
        // Default implementation - override in subclasses
    }

    /**
     * Set target setpoint for this mechanism
     * 
     * @param setpoint Target setpoint
     */
    public void setTargetSetpoint(SetPoint setpoint) {
        this.currentSetpoint = setpoint;
    }

    /**
     * Execute control for this mechanism This should be called periodically (e.g., every 20ms)
     */
    public void executeControl() {
        if (!motorIOs.isEmpty() && currentSetpoint != null) {
            // Calculate total feedforward for the mechanism
            SimpleMatrix totalFeedforward = getFeedforward(new SimpleMatrix(3, 1));

            // Distribute feedforward among motors based on their configuration
            List<Double> motorFeedforwards = distributeFeedforwardAmongMotors(totalFeedforward);

            // Send commands to all motors
            for (int i = 0; i < motorIOs.size(); i++) {
                MotorRequest request = new MotorRequest().withPosition(currentSetpoint.position)
                        .withVelocity(currentSetpoint.velocity)
                        .withAcceleration(currentSetpoint.acceleration)
                        .withFeedforward(motorFeedforwards.get(i));

                motorIOs.get(i).set(request);
            }
        }
    }

    /**
     * Check if mechanism is at target
     * 
     * @return true if mechanism is at target
     */
    public boolean isAtTarget() {
        if (motorIOs.isEmpty() || currentSetpoint == null) {
            return false;
        }

        // Check if all motors are at target (use average position/velocity)
        double avgPosition = 0.0;
        double avgVelocity = 0.0;

        for (MotorInputs inputs : motorInputs) {
            avgPosition += inputs.position;
            avgVelocity += inputs.velocity;
        }

        avgPosition /= motorInputs.size();
        avgVelocity /= motorInputs.size();

        // Check position tolerance
        double positionError = Math.abs(avgPosition - currentSetpoint.position);
        if (positionError > positionTolerance) {
            return false;
        }

        // Check velocity tolerance
        double velocityError = Math.abs(avgVelocity - currentSetpoint.velocity);
        if (velocityError > velocityTolerance) {
            return false;
        }

        return true;
    }

    /**
     * Emergency stop the mechanism
     */
    public void emergencyStop() {
        if (!motorIOs.isEmpty()) {
            MotorRequest stopRequest = new MotorRequest().withPosition(0.0).withVelocity(0.0)
                    .withAcceleration(0.0).withFeedforward(0.0);

            for (MotorIO motorIO : motorIOs) {
                motorIO.set(stopRequest);
            }
        }
    }

    /**
     * Get current motor inputs
     * 
     * @return Current motor inputs
     */
    public List<MotorInputs> getMotorInputs() {
        return motorInputs;
    }

    /**
     * Get motor inputs for a specific motor
     * 
     * @param motorIndex Index of the motor
     * @return Motor inputs for the specified motor
     */
    public MotorInputs getMotorInputs(int motorIndex) {
        if (motorIndex >= 0 && motorIndex < motorInputs.size()) {
            return motorInputs.get(motorIndex);
        }
        return null;
    }

    /**
     * Get current setpoint
     * 
     * @return Current setpoint
     */
    public SetPoint getCurrentSetpoint() {
        return currentSetpoint;
    }

    /**
     * Set control parameters
     */
    public void setControlParameters(double positionTolerance, double velocityTolerance) {
        this.positionTolerance = positionTolerance;
        this.velocityTolerance = velocityTolerance;
    }

    @Override
    public void updateInputs(Tinputs inputs) {
        // Update mechanism state first
        updateMechanismState();
    }

    @Override
    public void setControl(Trequest request) {
        // Execute control
        executeControl();
    }

    /**
     * Distribute feedforward among multiple motors This method can be overridden by subclasses for
     * custom distribution logic
     * 
     * @param totalFeedforward Total feedforward for the mechanism
     * @return List of feedforward values for each motor
     */
    protected List<Double> distributeFeedforwardAmongMotors(SimpleMatrix totalFeedforward) {
        List<Double> motorFeedforwards = new ArrayList<>();

        if (motorIOs.isEmpty()) {
            return motorFeedforwards;
        }

        // Calculate total feedforward magnitude
        double totalMagnitude = 0.0;
        if (totalFeedforward != null && totalFeedforward.getNumRows() > 0) {
            totalMagnitude = Math.sqrt(totalFeedforward.get(0, 0) * totalFeedforward.get(0, 0)
                    + totalFeedforward.get(1, 0) * totalFeedforward.get(1, 0)
                    + totalFeedforward.get(2, 0) * totalFeedforward.get(2, 0));
        }

        // Default distribution: equal among all motors
        double feedforwardPerMotor = totalMagnitude / motorIOs.size();

        for (int i = 0; i < motorIOs.size(); i++) {
            motorFeedforwards.add(feedforwardPerMotor);
        }

        return motorFeedforwards;
    }

    /**
     * Get number of motors in this mechanism
     * 
     * @return Number of motors
     */
    public int getMotorCount() {
        return motorIOs.size();
    }



}
