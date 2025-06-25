// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.structure.mechanisms;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.structure.physics.physicalObject;
import frc.lib.structure.physics.physicalProperties;
import frc.lib.structure.*;

/** Add your docs here. */
public abstract class Mechanism<Tconfig extends ConfigIO, Tinputs extends inputsIO, Trequest extends RequestIO> 
    extends SubsystemBase 
    implements physicalObject<Tconfig>, sensor<Tinputs>, actuator<Trequest>{

    private String name;
    private ArrayList<physicalObject<ConfigIO>> attachingObjects = new ArrayList<>();
    private physicalProperties properties;

    public Mechanism(String name, physicalProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    public void addPhysicalObjects(ArrayList<physicalObject<ConfigIO>> objects){
        attachingObjects.addAll(objects);
    }

    public void addPhysicalObject(physicalObject<ConfigIO> object){
        attachingObjects.add(object);
    }

    public void removePhysicalObject(physicalObject<ConfigIO> object){
        attachingObjects.remove(object);
    }

    public ArrayList<physicalObject<ConfigIO>> getAttachingObjects(){
        return attachingObjects;
    }



    @Override
    public void setConfig(Tconfig config) {
        
    }

    @Override
    public String getDescription() {
        return "" + name; //TODO
    }

    @Override
    public physicalProperties getPhysicalProperties() {
        return this.properties;
    }

    @Override
    public SimpleMatrix getFeedforward(SimpleMatrix noninertialFrame) {
        return new SimpleMatrix(new double[]{0, 0, 0});
    }

    @Override
    public void updateInputs(Tinputs inputs) {
    }

    @Override
    public void setControl(Trequest request) {
    }




}
