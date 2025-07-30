package frc.lib.structure.motors;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.ctre.phoenix6.StatusSignal;

import frc.lib.structure.*;

public interface MotorIO extends sensor<MotorInputs>, actuator<MotorRequest>{
    default void set(MotorRequest request) {
        // Default implementation does nothing
    }
    default MotorInputs get() {
        return new MotorInputs();
    }
    default void updateInputs(MotorInputs inputs) {
        
    }

    default ArrayList<StatusSignal<?>> getStatusSignals() {
        return new ArrayList<>();
    }
    default void setConfig(MotorConfig config) {
        // Default implementation does nothing
    }
}
