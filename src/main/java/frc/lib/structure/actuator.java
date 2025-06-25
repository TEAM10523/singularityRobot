package frc.lib.structure;

public interface actuator<Tconfig, Trequest> extends device<Tconfig>{
    
    default void setControl(Trequest request){};
}
