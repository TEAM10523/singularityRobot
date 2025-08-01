package frc.lib.structure.mechanisms;

import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import frc.lib.structure.configBase;
import frc.lib.structure.inputBase;
import frc.lib.structure.requestBase;
import frc.lib.structure.motors.MotorConfig;
import frc.lib.structure.motors.MotorInputs;
import frc.lib.structure.physics.physicalProperties;

/**
 * Rotating mechanism class for describing rotating structures like arms Extends
 * Mechanism,
 * specialized for rotating motion
 */
public class RotatingMechanism<Tconfig extends configBase, Tinputs extends inputBase, Trequest extends requestBase>
        extends Mechanism<Tconfig, Tinputs, Trequest> {

    private double currentAngle; // Current angle (radians)
    private double angularVelocity; // Angular velocity (rad/s)
    private double angularAcceleration; // Angular acceleration (rad/s²)

    // Rotation axis information
    private SimpleMatrix rotationAxis; // Rotation axis vector
    private SimpleMatrix pivotPoint; // Rotation center point

    public RotatingMechanism(String name, physicalProperties properties, SimpleMatrix rotationAxis,
            SimpleMatrix pivotPoint) {
        super(name, properties);
        this.rotationAxis = rotationAxis;
        this.pivotPoint = pivotPoint;
        this.currentAngle = 0.0;
        this.angularVelocity = 0.0;
        this.angularAcceleration = 0.0;
    }

    /**
     * Set current angle
     * 
     * @param angle Angle (radians)
     */
    public void setCurrentAngle(double angle) {
        this.currentAngle = angle;
    }

    /**
     * Set angular velocity
     * 
     * @param angularVelocity Angular velocity (rad/s)
     */
    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    /**
     * Set angular acceleration
     * 
     * @param angularAcceleration Angular acceleration (rad/s²)
     */
    public void setAngularAcceleration(double angularAcceleration) {
        this.angularAcceleration = angularAcceleration;
    }

    /**
     * Get current angle
     * 
     * @return Current angle (radians)
     */
    public double getCurrentAngle() {
        return currentAngle;
    }

    /**
     * Get angular velocity
     * 
     * @return Angular velocity (rad/s)
     */
    public double getAngularVelocity() {
        return angularVelocity;
    }

    /**
     * Get angular acceleration
     * 
     * @return Angular acceleration (rad/s²)
     */
    public double getAngularAcceleration() {
        return angularAcceleration;
    }

    /**
     * Get rotation axis
     * 
     * @return Rotation axis vector
     */
    public SimpleMatrix getRotationAxis() {
        return rotationAxis;
    }

    /**
     * Get rotation center point
     * 
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
        // Update rotating mechanism state from motor inputs (average of all motors)
        if (getMotorCount() > 0) {
            double avgAngle = 0.0;
            double avgAngularVelocity = 0.0;
            double avgAngularAcceleration = 0.0;

            for (MotorInputs inputs : getMotorInputs()) {
                avgAngle += inputs.position;
                avgAngularVelocity += inputs.velocity;
                avgAngularAcceleration += inputs.acceleration;
            }

            avgAngle /= getMotorCount();
            avgAngularVelocity /= getMotorCount();
            avgAngularAcceleration /= getMotorCount();

            setCurrentAngle(avgAngle);
            setAngularVelocity(avgAngularVelocity);
            setAngularAcceleration(avgAngularAcceleration);
        }
    }

    /**
     * Calculate feedforward torque for rotating mechanism Includes gravity torque,
     * inertia torque,
     * etc.
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
        SimpleMatrix gravity = new SimpleMatrix(3, 1, true, 0.0, 0.0, -9.81 * getPhysicalProperties().mass);

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

        return new SimpleMatrix(3, 1, true, ay * bz - az * by, az * bx - ax * bz,
                ax * by - ay * bx);
    }

    /**
     * Override feedforward distribution for rotating mechanisms Consider gear
     * ratios and motor
     * efficiency for better distribution
     */
    @Override
    protected List<Double> distributeFeedforwardAmongMotors(SimpleMatrix totalFeedforward) {
        List<Double> motorFeedforwards = new ArrayList<>();

        if (getMotorCount() == 0) {
            return motorFeedforwards;
        }

        // Calculate total feedforward magnitude (torque)
        double totalMagnitude = 0.0;
        if (totalFeedforward != null && totalFeedforward.getNumRows() > 0) {
            totalMagnitude = Math.sqrt(totalFeedforward.get(0, 0) * totalFeedforward.get(0, 0)
                    + totalFeedforward.get(1, 0) * totalFeedforward.get(1, 0)
                    + totalFeedforward.get(2, 0) * totalFeedforward.get(2, 0));
        }

        // Calculate total gear ratio and efficiency
        double totalGearRatio = 0.0;

        for (MotorConfig config : motorConfigs) {
            totalGearRatio += config.gearRatio;
        }

        // Distribute torque based on gear ratio and efficiency
        for (int i = 0; i < getMotorCount(); i++) {
            MotorConfig config = motorConfigs.get(i);
            double motorRatio = config.gearRatio / totalGearRatio;
            double motorEfficiency = 0.85; // Individual motor efficiency

            // Torque = total torque * motor ratio / motor efficiency
            double motorTorque = totalMagnitude * motorRatio / motorEfficiency;
            motorFeedforwards.add(motorTorque);
        }

        return motorFeedforwards;
    }
}
