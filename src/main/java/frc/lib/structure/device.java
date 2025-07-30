package frc.lib.structure;

public interface device<Tconfig extends configBase> {

    default void setConfig(Tconfig config){}

    default String getDescription(){return null;};
    
}
