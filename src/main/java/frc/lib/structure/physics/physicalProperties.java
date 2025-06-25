// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.structure.physics;

import java.util.Optional;

import org.ejml.simple.SimpleMatrix;

/** Add your docs here. */
public class physicalProperties {

    public double mass;
    public SimpleMatrix CG;
    public SimpleMatrix MOI;
    
    public Optional<material> material;
    public physicalProperties(double mass, SimpleMatrix CG, SimpleMatrix MOI, Optional<material> material){
        this.mass = mass;
        this.CG = CG;
        this.MOI = MOI;
        this.material = material;
    }
}
