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
 * Linear mechanism class for describing linear motion structures like elevators Extends Mechanism,
 * specialized for linear motion
 */
public class LinearMechanism<Tconfig extends configBase, Tinputs extends inputBase, Trequest extends requestBase>
        extends Mechanism<Tconfig, Tinputs, Trequest> {

    private double currentPosition; // Current position (meters)
    private double velocity; // Velocity (m/s)
    private double acceleration; // Acceleration (m/s²)

    // Motion axis information
    private SimpleMatrix motionAxis; // Motion axis vector
    private SimpleMatrix startPoint; // Starting point

    public LinearMechanism(String name, physicalProperties properties, SimpleMatrix motionAxis,
            SimpleMatrix startPoint) {
        super(name, properties);
        this.motionAxis = motionAxis;
        this.startPoint = startPoint;
        this.currentPosition = 0.0;
        this.velocity = 0.0;
        this.acceleration = 0.0;
    }

    /**
     * Set current position
     * 
     * @param position Position (meters)
     */
    public void setCurrentPosition(double position) {
        this.currentPosition = position;
    }

    /**
     * Set velocity
     * 
     * @param velocity Velocity (m/s)
     */
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    /**
     * Set acceleration
     * 
     * @param acceleration Acceleration (m/s²)
     */
    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Get current position
     * 
     * @return Current position (meters)
     */
    public double getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Get velocity
     * 
     * @return Velocity (m/s)
     */
    public double getVelocity() {
        return velocity;
    }

    /**
     * Get acceleration
     * 
     * @return Acceleration (m/s²)
     */
    public double getAcceleration() {
        return acceleration;
    }

    /**
     * Get motion axis
     * 
     * @return Motion axis vector
     */
    public SimpleMatrix getMotionAxis() {
        return motionAxis;
    }

    /**
     * Get starting point
     * 
     * @return Starting point
     */
    public SimpleMatrix getStartPoint() {
        return startPoint;
    }

    /**
     * Get current center of mass position
     * 
     * @return Center of mass position
     */
    public SimpleMatrix getCurrentCenterOfMass() {
        // Center of mass = start point + current position * motion axis direction
        return startPoint.plus(motionAxis.scale(currentPosition));
    }

    @Override
    public void setConfig(Tconfig config) {
        // Implement linear mechanism specific configuration
        super.setConfig(config);
    }

    @Override
    protected void updateStateFromMotorInputs() {
        // Update linear mechanism state from motor inputs (average of all motors)
        if (getMotorCount() > 0) {
            double avgPosition = 0.0;
            double avgVelocity = 0.0;
            double avgAcceleration = 0.0;

            for (MotorInputs inputs : getMotorInputs()) {
                avgPosition += inputs.position;
                avgVelocity += inputs.velocity;
                avgAcceleration += inputs.acceleration;
            }

            avgPosition /= getMotorCount();
            avgVelocity /= getMotorCount();
            avgAcceleration /= getMotorCount();

            setCurrentPosition(avgPosition);
            setVelocity(avgVelocity);
            setAcceleration(avgAcceleration);
        }
    }

    /**
     * Calculate feedforward force for linear mechanism Includes gravity, inertia, friction, etc.
     */
    @Override
    public SimpleMatrix getFeedforward(SimpleMatrix noninertialFrame) {
        // Calculate gravity
        SimpleMatrix gravity = calculateGravity();

        // Calculate inertia
        SimpleMatrix inertia = calculateInertia();

        // Calculate friction (simplified model)
        SimpleMatrix friction = calculateFriction();

        // Return total feedforward force
        return gravity.plus(inertia).plus(friction);
    }

    /**
     * Calculate gravity
     */
    private SimpleMatrix calculateGravity() {
        // Gravity vector (assume gravity direction is -z)
        return new SimpleMatrix(3, 1, true, 0.0, 0.0, -9.81 * getPhysicalProperties().mass);
    }

    /**
     * Calculate inertia
     */
    private SimpleMatrix calculateInertia() {
        // F = m * a
        return motionAxis.scale(getPhysicalProperties().mass * acceleration);
    }

    /**
     * Calculate friction (simplified model)
     */
    private SimpleMatrix calculateFriction() {
        // Simplified friction model: opposite to velocity direction, proportional to velocity
        double frictionCoefficient = 0.1; // Friction coefficient
        double frictionMagnitude = frictionCoefficient * Math.abs(velocity);

        if (velocity > 0) {
            return motionAxis.scale(-frictionMagnitude);
        } else if (velocity < 0) {
            return motionAxis.scale(frictionMagnitude);
        } else {
            return new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Override feedforward distribution for linear mechanisms Consider gear ratios and motor
     * efficiency for better distribution
     */
    @Override
    protected List<Double> distributeFeedforwardAmongMotors(SimpleMatrix totalFeedforward) {
        List<Double> motorFeedforwards = new ArrayList<>();

        if (getMotorCount() == 0) {
            return motorFeedforwards;
        }

        // Calculate total feedforward magnitude
        double totalMagnitude = 0.0;
        if (totalFeedforward != null && totalFeedforward.getNumRows() > 0) {
            totalMagnitude = Math.sqrt(totalFeedforward.get(0, 0) * totalFeedforward.get(0, 0)
                    + totalFeedforward.get(1, 0) * totalFeedforward.get(1, 0)
                    + totalFeedforward.get(2, 0) * totalFeedforward.get(2, 0));
        }

        // Calculate total gear ratio and efficiency
        double totalGearRatio = 0.0;
        double totalEfficiency = 0.0;

        for (MotorConfig config : motorConfigs) {
            totalGearRatio += config.gearRatio;
            totalEfficiency += 0.85; // Assume 85% efficiency per motor
        }

        // Distribute based on gear ratio and efficiency
        for (int i = 0; i < getMotorCount(); i++) {
            MotorConfig config = motorConfigs.get(i);
            double motorRatio = config.gearRatio / totalGearRatio;
            double motorEfficiency = 0.85; // Individual motor efficiency

            // Feedforward = total force * motor ratio / motor efficiency
            double motorFeedforward = totalMagnitude * motorRatio / motorEfficiency;
            motorFeedforwards.add(motorFeedforward);
        }

        return motorFeedforwards;
    }
}
