package frc.lib.structure.mechanisms;

/**
 * SetPoint class for mechanism control Represents target position, velocity, acceleration and
 * feedforward
 */
public class SetPoint {
    public double position;
    public double velocity;
    public double acceleration;
    public double feedforward;

    public SetPoint(double position, double velocity, double acceleration, double feedforward) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.feedforward = feedforward;
    }
}
