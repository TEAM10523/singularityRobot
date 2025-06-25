package frc.lib.structure.motors;

import frc.lib.structure.RequestIO;

public class MotorRequest implements RequestIO{
    
    public double position = 0;
    public double velocity = 0;
    public double acceleration = 0;
    public double feedforward = 0;

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
