package frc.lib.structure;

public interface sensor<Tinputs extends inputsIO>{
    
    default void updateInputs(Tinputs inputs){};
}
