package frc.lib.structure.motors;

import frc.lib.structure.requestBase;

public class MotorRequest extends requestBase{
    
    public double position;
    public double velocity;
    public double acceleration;
    public double feedforward;

    public MotorRequest(){}
    
    public MotorRequest withPosition(double position){
        this.position = position;
        return this;
    }

    public MotorRequest withVelocity(double velocity){
        this.velocity = velocity;
        return this;
    }

    public MotorRequest withAcceleration(double acceleration){
        this.acceleration = acceleration;
        return this;
    }

    public MotorRequest withFeedforward(double feedforward){
        this.feedforward = feedforward;
        return this;
    }
}
