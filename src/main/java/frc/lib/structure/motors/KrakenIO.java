package frc.lib.structure.motors;

import java.io.ObjectInputFilter.Status;
import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.AutoLog;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.StatusSignal.SignalMeasurement;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.fasterxml.jackson.databind.JsonSerializable.Base;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;

public class KrakenIO implements MotorIO{
    TalonFX motor;
    MotorConfig config;     
    StatusSignal<Angle> position;
    SignalMeasurement<Angle> referenceSignalPosition;
    StatusSignal<AngularVelocity> velocity;
    StatusSignal<Voltage> appliedVolts;
    StatusSignal<Current> supplyCurrent;
    StatusSignal<Current> torqueCurrent;
    StatusSignal<AngularAcceleration> acceleration;
    StatusSignal<Temperature> temperature;
    PositionTorqueCurrentFOC positionTorqueCurrentFOC=new PositionTorqueCurrentFOC(0.);
    VelocityTorqueCurrentFOC velocityTorqueCurrentFOC=new VelocityTorqueCurrentFOC(0.);
    public MotorInputs inputs=new MotorInputs();
    public KrakenIO(MotorConfig config) {
        setConfig(config);
    }

    @Override
    public void set(MotorRequest request) {
        if(!Double.isNaN(request.position))
        motor.setControl(positionTorqueCurrentFOC.withPosition(request.position/ (2. * Math.PI))
                .withVelocity(request.velocity/ (2. * Math.PI))
                .withFeedForward(request.feedforward*config.kT));
        else
        motor.setControl(velocityTorqueCurrentFOC.withVelocity(request.velocity/ (2. * Math.PI)).withAcceleration(request.acceleration/ (2. * Math.PI))
                .withFeedForward(request.feedforward*config.kT));
        
    }
    @Override
    public void setConfig(MotorConfig config) {
        motor=new TalonFX(config.ID, config.canbus);
        this.config=config;
        var talonFXConfigurator= motor.getConfigurator();
        var talonFXConfigs=new TalonFXConfiguration();
        talonFXConfigs.Slot0.kP = config.kP;
        talonFXConfigs.Slot0.kI = config.kI;
        talonFXConfigs.Slot0.kD = config.kD;
        talonFXConfigs.Slot0.kS = config.kS;
        talonFXConfigs.Slot0.kG = config.kG;
        talonFXConfigs.Slot0.kV = config.kV;
        talonFXConfigs.Slot0.kA = config.kA;
        talonFXConfigs.MotionMagic.MotionMagicAcceleration = config.maxAcc;
        talonFXConfigs.MotionMagic.MotionMagicCruiseVelocity = config.maxVel;
        talonFXConfigs.Feedback.SensorToMechanismRatio = config.gearRatio;
        talonFXConfigs.MotorOutput.Inverted = config.reversed? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
        talonFXConfigs.MotorOutput.NeutralMode = config.isBreak ? NeutralModeValue.Brake : NeutralModeValue.Coast;
        talonFXConfigs.CurrentLimits.SupplyCurrentLimitEnable= config.supplyCurrentLimitEnabled ;
        talonFXConfigs.CurrentLimits.SupplyCurrentLimit= config.supplyCurrentLimit;
        talonFXConfigs.CurrentLimits.SupplyCurrentLowerTime = config.supplyCurrentLimitLowerTime;
        talonFXConfigs.CurrentLimits.StatorCurrentLimitEnable= config.statorCurrentLimitEnabled ;
        talonFXConfigs.CurrentLimits.StatorCurrentLimit= config.statorCurrentLimit ;
        talonFXConfigurator.apply(talonFXConfigs);
        position = motor.getPosition();
        referenceSignalPosition=position.getDataCopy();
        velocity = motor.getVelocity();
        appliedVolts = motor.getMotorVoltage();
        supplyCurrent = motor.getSupplyCurrent();
        torqueCurrent = motor.getTorqueCurrent();
        acceleration = motor.getAcceleration();
        temperature = motor.getDeviceTemp();
        BaseStatusSignal.setUpdateFrequencyForAll(config.updateFrequency, position, velocity, appliedVolts, supplyCurrent, torqueCurrent, acceleration,temperature);
        // BaseStatusSignal.waitForAll(0, null)
        // motor.optimizeBusUtilization();
    }
    @Override
    public void updateInputs(MotorInputs inputs) {
        
        inputs.connected = motor.isConnected();
        if(config.isInnerSyncronized)
            BaseStatusSignal.waitForAll(0.010, position, velocity, appliedVolts, supplyCurrent, torqueCurrent, acceleration, temperature);
        inputs.position = position.getValueAsDouble();
        inputs.velocity = velocity.getValueAsDouble();
        inputs.current = supplyCurrent.getValueAsDouble();
        inputs.acceleration = acceleration.getValueAsDouble();
        inputs.temperature=temperature.getValueAsDouble();
        inputs.latency=position.getAllTimestamps().getBestTimestamp().getLatency();
    }
    @Override
        public
        ArrayList<StatusSignal<?>> getStatusSignals() {
            return new ArrayList<>(List.of(position, velocity, appliedVolts, supplyCurrent, torqueCurrent, acceleration, temperature));
        }
    @Override
    public MotorInputs get() {
        // Implement Kraken-specific logic to retrieve motor inputs here
        return inputs;
    }
}
