package frc.lib.structure.motors;

import frc.lib.structure.ConfigIO;

public class MotorConfig implements ConfigIO {
    // basic configs
    public String name;
    public int ID;
    public String canbus;
    
    // PIDs and FFs
    public double kP = 0;
    public double kI = 0;
    public double kD = 0;
    public double kS = 0;
    public double kG = 0;
    public double kV = 0;
    public double kA = 0;

    public double kT = 0;

    // basic motion profile
    public double maxVel;
    public double maxAcc;

    // physics
    public double gearRatio = 1.0;
    public boolean reversed = false;
    public boolean isBreak = false;
    public boolean continuous = false;

    public double stallTorque = 0;
    public double freeVel = 0;
    
    // supply current
    public boolean supplyCurrentLimitEnabled = false;
    public double supplyCurrentLimit = 0;
    public double supplyCurrentLowerLimit = 0;
    public double supplyCurrentLimitLowerTime = 0;
    
    // stator current
    public boolean enabledFOC = false;

    public boolean statorCurrentLimitEnabled = false;
    public double statorCurrentLimit = 0;
    public double statorCurrentLimitOffset = 0;

    public MotorConfig(String name, int ID, String canbus) {
        this.name = name;
        this.ID = ID;
        this.canbus = canbus;
    }
}
