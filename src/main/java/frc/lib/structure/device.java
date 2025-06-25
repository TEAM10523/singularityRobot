package frc.lib.structure;

public interface device<Tconfig> {

    default void setConfig(Tconfig config){}

    default String getDescription(){return null;};
    
}
