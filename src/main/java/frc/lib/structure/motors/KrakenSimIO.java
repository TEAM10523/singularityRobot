package frc.lib.structure.motors;

import java.util.ArrayList;

import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.math.MathUtil;

/**
 * Simulated Kraken motor implementation for testing and simulation
 * Uses simplified physics model and AdvantageKit logging
 */
public class KrakenSimIO implements MotorIO {

    private MotorConfig config;
    private MotorInputs inputs = new MotorInputs();

    // Simulation state
    private double position = 0.0;
    private double velocity = 0.0;
    private double acceleration = 0.0;
    private double current = 0.0;
    private double temperature = 25.0;
    private double lastTime = 0.0;

    // Control inputs
    private double targetPosition = 0.0;
    private double targetVelocity = 0.0;
    private double targetAcceleration = 0.0;
    private double feedforward = 0.0;

    // PID controller for position control
    private double positionError = 0.0;
    private double velocityError = 0.0;
    private double integralError = 0.0;
    private double lastError = 0.0;

    // Simulation parameters
    private static final double SIMULATION_DT = 0.02; // 20ms
    private static final double MAX_VOLTAGE = 12.0;
    private static final double MIN_VOLTAGE = -12.0;
    private static final double MOTOR_RESISTANCE = 0.1; // ohms
    private static final double MOTOR_KV = 100.0; // RPM/volt
    private static final double MOTOR_KT = 0.1; // Nm/amp

    public KrakenSimIO(MotorConfig config) {
        setConfig(config);
    }

    @Override
    public void setConfig(MotorConfig config) {
        this.config = config;

        // Initialize simulation state
        lastTime = System.currentTimeMillis() / 1000.0;
        position = 0.0;
        velocity = 0.0;
        acceleration = 0.0;
        current = 0.0;
        temperature = 25.0;

        Logger.recordOutput("Motor/" + config.name + "/Config", config.toString());
    }

    @Override
    public void set(MotorRequest request) {
        targetPosition = request.position;
        targetVelocity = request.velocity;
        targetAcceleration = request.acceleration;
        feedforward = request.feedforward;

        // Log control inputs
        Logger.recordOutput("Motor/" + config.name + "/TargetPosition", targetPosition);
        Logger.recordOutput("Motor/" + config.name + "/TargetVelocity", targetVelocity);
        Logger.recordOutput("Motor/" + config.name + "/TargetAcceleration", targetAcceleration);
        Logger.recordOutput("Motor/" + config.name + "/Feedforward", feedforward);
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        double currentTime = System.currentTimeMillis() / 1000.0;
        double dt = currentTime - lastTime;

        if (dt < SIMULATION_DT) {
            // Not enough time has passed, return current state
            return;
        }

        // Calculate control output (desired current in FOC)
        double desiredCurrent = calculateControlOutput();

        // Apply current control to motor simulation
        // In FOC, we directly control the current (torque current)
        simulateMotor(desiredCurrent, dt);

        // Update inputs
        inputs.connected = true;
        inputs.position = position;
        inputs.velocity = velocity;
        inputs.acceleration = acceleration;
        inputs.current = current;
        inputs.temperature = temperature;
        inputs.latency = dt * 1000.0; // Convert to milliseconds

        // Update last time
        lastTime = currentTime;

        // Log all motor data
        logMotorData(inputs, desiredCurrent, desiredCurrent);
    }

    private void simulateMotor(double desiredCurrent, double dt) {
        // FOC motor simulation: direct current control
        // In FOC, we directly control the torque current (q-axis current)

        // Apply current limits
        double maxCurrent = Math.min(config.supplyCurrentLimit, config.statorCurrentLimit);
        current = MathUtil.clamp(desiredCurrent, -maxCurrent, maxCurrent);

        // Calculate torque from current (FOC: τ = Kt * Iq)
        double torque = current * MOTOR_KT;

        // Apply gear ratio
        double outputTorque = torque * config.gearRatio;

        // Calculate load inertia (including motor rotor inertia)
        double motorInertia = 0.001; // kg⋅m² (typical for Kraken)
        double loadInertia = 0.1; // kg⋅m²
        double totalInertia = motorInertia + loadInertia;

        // Calculate acceleration from torque
        acceleration = outputTorque / totalInertia;

        // Update velocity and position
        velocity += acceleration * dt;
        position += velocity * dt;

        // Add viscous damping (friction)
        double dampingCoefficient = 0.1; // N⋅m⋅s/rad
        double dampingTorque = -dampingCoefficient * velocity;
        velocity += (dampingTorque / totalInertia) * dt;

        // Update temperature based on current (simplified thermal model)
        double thermalResistance = 0.1; // °C/W
        double powerLoss = current * current * MOTOR_RESISTANCE; // I²R losses
        double temperatureRise = powerLoss * thermalResistance;
        temperature = 25.0 + temperatureRise;
    }

    private double calculateControlOutput() {
        // Calculate position and velocity errors
        positionError = targetPosition - position;
        velocityError = targetVelocity - velocity;

        // Anti-windup for integral term
        if (Math.abs(positionError) < 0.1) {
            integralError += positionError * SIMULATION_DT;
        } else {
            integralError = 0.0;
        }

        // Clamp integral term
        integralError = MathUtil.clamp(integralError, -1.0, 1.0);

        // PID control with velocity feedback
        // P term: position error
        // I term: position error integral
        // D term: velocity error (rate of change of position error)
        double pidOutput = config.kP * positionError +
                config.kI * integralError +
                config.kD * velocityError;

        // Add feedforward and target acceleration
        // In FOC, this output represents the desired current (torque current)
        double desiredCurrent = pidOutput + feedforward * config.kT;
        // + targetAcceleration * 0.1;

        // Update last error for derivative calculation
        lastError = positionError;

        return desiredCurrent;
    }

    private void logMotorData(MotorInputs inputs, double desiredCurrent, double controlOutput) {
        String motorName = config.name;

        // Log motor state
        Logger.recordOutput("Motor/" + motorName + "/Position", inputs.position);
        Logger.recordOutput("Motor/" + motorName + "/Velocity", inputs.velocity);
        Logger.recordOutput("Motor/" + motorName + "/Acceleration", inputs.acceleration);
        Logger.recordOutput("Motor/" + motorName + "/Current", inputs.current);
        Logger.recordOutput("Motor/" + motorName + "/Temperature", inputs.temperature);
        Logger.recordOutput("Motor/" + motorName + "/Latency", inputs.latency);
        Logger.recordOutput("Motor/" + motorName + "/Connected", inputs.connected);

        // Log control data (FOC current control)
        Logger.recordOutput("Motor/" + motorName + "/DesiredCurrent", desiredCurrent);
        Logger.recordOutput("Motor/" + motorName + "/ControlOutput", controlOutput);
        Logger.recordOutput("Motor/" + motorName + "/PositionError", positionError);
        Logger.recordOutput("Motor/" + motorName + "/VelocityError", velocityError);
        Logger.recordOutput("Motor/" + motorName + "/IntegralError", integralError);

        // Log PID gains
        Logger.recordOutput("Motor/" + motorName + "/kP", config.kP);
        Logger.recordOutput("Motor/" + motorName + "/kI", config.kI);
        Logger.recordOutput("Motor/" + motorName + "/kD", config.kD);
        Logger.recordOutput("Motor/" + motorName + "/GearRatio", config.gearRatio);

        // Log motor limits
        Logger.recordOutput("Motor/" + motorName + "/MaxVelocity", config.maxVel);
        Logger.recordOutput("Motor/" + motorName + "/MaxAcceleration", config.maxAcc);
        Logger.recordOutput("Motor/" + motorName + "/SupplyCurrentLimit", config.supplyCurrentLimit);
        Logger.recordOutput("Motor/" + motorName + "/StatorCurrentLimit", config.statorCurrentLimit);
    }

    @Override
    public MotorInputs get() {
        return inputs;
    }

    @Override
    public ArrayList<StatusSignal<?>> getStatusSignals() {
        // Return empty list for simulation
        return new ArrayList<>();
    }

    /**
     * Get motor configuration
     */
    public MotorConfig getConfig() {
        return config;
    }

    /**
     * Reset simulation state
     */
    public void reset() {
        position = 0.0;
        velocity = 0.0;
        acceleration = 0.0;
        current = 0.0;
        temperature = 25.0;
        lastTime = System.currentTimeMillis() / 1000.0;
        positionError = 0.0;
        velocityError = 0.0;
        integralError = 0.0;
        lastError = 0.0;
        targetPosition = 0.0;
        targetVelocity = 0.0;
        targetAcceleration = 0.0;
        feedforward = 0.0;
    }
}