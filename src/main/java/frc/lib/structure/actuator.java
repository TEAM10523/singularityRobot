package frc.lib.structure;

public interface actuator<Trequest extends RequestIO>{
    
    default void setControl(Trequest request){};
}
