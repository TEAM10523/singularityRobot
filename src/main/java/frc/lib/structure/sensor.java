package frc.lib.structure;

public interface sensor<Tconfig, Tinputs> extends device<Tconfig>{
    
    default void updateInputs(Tinputs inputs){};
}
