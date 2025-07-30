package frc.lib.structure;

public interface sensor<Tinputs extends inputBase>{
    
    default void updateInputs(Tinputs inputs){};
}
