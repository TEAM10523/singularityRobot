package frc.lib.structure;

public interface device<Tconfig extends ConfigIO> {

    default void setConfig(Tconfig config){}

    default String getDescription(){return null;};
    
}
