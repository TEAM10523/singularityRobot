package frc.lib.structure;

public interface actuator<Trequest extends requestBase>{
    
    default void setControl(Trequest request){};
}
