package frc.lib.structure.mechanisms;

import frc.lib.structure.configBase;
import frc.lib.structure.requestBase;
import frc.lib.structure.inputBase;
import frc.lib.structure.physics.physicalProperties;
import frc.lib.structure.motors.MotorInputs;
import org.ejml.simple.SimpleMatrix;

/**
 * Rotating mechanism class for describing rotating structures like arms
 * Extends Mechanism, specialized for rotating motion
 */
public class RotatingMechanism<Tconfig extends configBase, Tinputs extends inputBase, Trequest extends requestBase> 
    extends Mechanism<Tconfig, Tinputs, Trequest> {

    private double currentAngle; // Current angle (radians)
    private double angularVelocity; // Angular velocity (rad/s)
    private double angularAcceleration; // Angular acceleration (rad/s²)
    
    // Rotation axis information
    private SimpleMatrix rotationAxis; // Rotation axis vector
    private SimpleMatrix pivotPoint; // Rotation center point

    public RotatingMechanism(String name, physicalProperties properties, 
                           SimpleMatrix rotationAxis, SimpleMatrix pivotPoint) {
        super(name, properties);
        this.rotationAxis = rotationAxis;
        this.pivotPoint = pivotPoint;
        this.currentAngle = 0.0;
        this.angularVelocity = 0.0;
        this.angularAcceleration = 0.0;
    }

    /**
     * Set current angle
     * @param angle Angle (radians)
     */
    public void setCurrentAngle(double angle) {
        this.currentAngle = angle;
    }

    /**
     * Set angular velocity
     * @param angularVelocity Angular velocity (rad/s)
     */
    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    /**
     * Set angular acceleration
     * @param angularAcceleration Angular acceleration (rad/s²)
     */
    public void setAngularAcceleration(double angularAcceleration) {
        this.angularAcceleration = angularAcceleration;
    }

    /**
     * Get current angle
     * @return Current angle (radians)
     */
    public double getCurrentAngle() {
        return currentAngle;
    }

    /**
     * Get angular velocity
     * @return Angular velocity (rad/s)
     */
    public double getAngularVelocity() {
        return angularVelocity;
    }

    /**
     * Get angular acceleration
     * @return Angular acceleration (rad/s²)
     */
    public double getAngularAcceleration() {
        return angularAcceleration;
    }

    /**
     * Get rotation axis
     * @return Rotation axis vector
     */
    public SimpleMatrix getRotationAxis() {
        return rotationAxis;
    }

    /**
     * Get rotation center point
     * @return Rotation center point
     */
    public SimpleMatrix getPivotPoint() {
        return pivotPoint;
    }

    @Override
    public void setConfig(Tconfig config) {
        // Implement rotating mechanism specific configuration
        super.setConfig(config);
    }

    @Override
    protected void updateStateFromMotorInputs() {
        // Update rotating mechanism state from motor inputs
        MotorInputs currentInputs = motorIO.get();
        setCurrentAngle(currentInputs.position);
        setAngularVelocity(currentInputs.velocity);
        setAngularAcceleration(currentInputs.acceleration);
    }

    /**
     * Calculate feedforward torque for rotating mechanism
     * Includes gravity torque, inertia torque, etc.
     */
    @Override
    public SimpleMatrix getFeedforward(SimpleMatrix noninertialFrame) {
        // Calculate gravity torque
        SimpleMatrix gravityTorque = calculateGravityTorque();
        
        // Calculate inertia torque
        SimpleMatrix inertiaTorque = calculateInertiaTorque();
        
        // Calculate coriolis torque
        SimpleMatrix coriolisTorque = calculateCoriolisTorque();
        
        // Return total feedforward torque
        return gravityTorque.plus(inertiaTorque).plus(coriolisTorque);
    }

    /**
     * Calculate gravity torque
     */
    private SimpleMatrix calculateGravityTorque() {
        // Get center of mass position
        SimpleMatrix cg = getPhysicalProperties().CG;
        
        // Calculate gravity vector (assume gravity direction is -z)
        SimpleMatrix gravity = new SimpleMatrix(3, 1, true, 
            0.0, 0.0, -9.81 * getPhysicalProperties().mass);
        
        // Calculate gravity torque = r × F
        SimpleMatrix r = cg.minus(pivotPoint);
        return crossProduct(r, gravity);
    }

    /**
     * Calculate inertia torque
     */
    private SimpleMatrix calculateInertiaTorque() {
        // τ = I * α
        SimpleMatrix moi = getPhysicalProperties().MOI;
        SimpleMatrix alpha = rotationAxis.scale(angularAcceleration);
        return moi.mult(alpha);
    }

    /**
     * Calculate coriolis torque
     */
    private SimpleMatrix calculateCoriolisTorque() {
        // Coriolis torque = ω × (I * ω)
        SimpleMatrix moi = getPhysicalProperties().MOI;
        SimpleMatrix omega = rotationAxis.scale(angularVelocity);
        SimpleMatrix Iw = moi.mult(omega);
        return crossProduct(omega, Iw);
    }

    /**
     * Vector cross product calculation
     */
    private SimpleMatrix crossProduct(SimpleMatrix a, SimpleMatrix b) {
        double ax = a.get(0, 0);
        double ay = a.get(1, 0);
        double az = a.get(2, 0);
        
        double bx = b.get(0, 0);
        double by = b.get(1, 0);
        double bz = b.get(2, 0);
        
        return new SimpleMatrix(3, 1, true,
            ay * bz - az * by,
            az * bx - ax * bz,
            ax * by - ay * bx);
    }
} 