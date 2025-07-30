// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.structure.mechanisms;

import java.util.ArrayList;
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

    // Motor control
    protected MotorIO motorIO;
    protected MotorConfig motorConfig;
    protected MotorInputs motorInputs;
    protected SetPoint currentSetpoint;

    // Control parameters
    protected double positionTolerance = 0.01; // meters or radians
    protected double velocityTolerance = 0.1; // m/s or rad/s



    public Mechanism(String name, physicalProperties properties) {
        this.name = name;
        this.properties = properties;
        this.motorInputs = new MotorInputs();
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
        this.motorIO = motorIO;
        this.motorConfig = config;
        motorIO.setConfig(config);
    }

    /**
     * Update mechanism state from motor feedback This should be called periodically (e.g., every
     * 20ms)
     */
    public void updateMechanismState() {
        if (motorIO != null) {
            motorIO.updateInputs(motorInputs);
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
        if (motorIO != null && currentSetpoint != null) {
            // Calculate feedforward
            SimpleMatrix feedforward = getFeedforward(new SimpleMatrix(3, 1));
            double feedforwardValue = 0.0;
            if (feedforward != null && feedforward.getNumRows() > 0) {
                feedforwardValue = Math.sqrt(feedforward.get(0, 0) * feedforward.get(0, 0)
                        + feedforward.get(1, 0) * feedforward.get(1, 0)
                        + feedforward.get(2, 0) * feedforward.get(2, 0));
            }

            // Create motor request
            MotorRequest request = new MotorRequest().withPosition(currentSetpoint.position)
                    .withVelocity(currentSetpoint.velocity)
                    .withAcceleration(currentSetpoint.acceleration)
                    .withFeedforward(feedforwardValue);

            // Send command to motor
            motorIO.set(request);
        }
    }

    /**
     * Check if mechanism is at target
     * 
     * @return true if mechanism is at target
     */
    public boolean isAtTarget() {
        if (motorIO == null || currentSetpoint == null) {
            return false;
        }

        // Get current motor state
        MotorInputs currentInputs = motorIO.get();

        // Check position tolerance
        double positionError = Math.abs(currentInputs.position - currentSetpoint.position);
        if (positionError > positionTolerance) {
            return false;
        }

        // Check velocity tolerance
        double velocityError = Math.abs(currentInputs.velocity - currentSetpoint.velocity);
        if (velocityError > velocityTolerance) {
            return false;
        }

        return true;
    }

    /**
     * Emergency stop the mechanism
     */
    public void emergencyStop() {
        if (motorIO != null) {
            MotorRequest stopRequest = new MotorRequest().withPosition(0.0).withVelocity(0.0)
                    .withAcceleration(0.0).withFeedforward(0.0);
            motorIO.set(stopRequest);
        }
    }

    /**
     * Get current motor inputs
     * 
     * @return Current motor inputs
     */
    public MotorInputs getMotorInputs() {
        return motorInputs;
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



}
