package frc.lib.structure.motors;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;
import frc.lib.structure.inputBase;
import frc.lib.structure.inputsIO;

public class MotorInputs extends inputBase implements inputsIO, LoggableInputs {
    public boolean connected;
    public double position;
    public double velocity;
    public double current;
    public double acceleration;
    public double latency;
    public double temperature;

    public void toLog(LogTable table) {
        table.put("connected", connected);
        table.put("position", position);
        table.put("velocity", velocity);
        table.put("current", current);
        table.put("acceleration", acceleration);
        table.put("latency", latency);
        table.put("temperature", temperature);
    }

    public void fromLog(LogTable table) {
        connected = table.get("connected", connected);
        position = table.get("position", position);
        velocity = table.get("velocity", velocity);
        current = table.get("current", current);
        acceleration = table.get("acceleration", acceleration);
        latency = table.get("latency", latency);
        temperature = table.get("temperature", temperature);

    }
}
