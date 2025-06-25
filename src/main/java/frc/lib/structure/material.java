// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.structure;

/**
 * A material class for Isaac Sim simulations that defines physical properties 
 * for objects in the simulation environment.
 */
public class material {
    public double friction;
    public double restitution;
    public double damping;

    /**
     * Constructs a new Material with specified physical properties.
     * 
     * @param friction Coefficient of friction (0-1)
     * @param restitution Coefficient of restitution (0-1)
     * @param damping Damping factor (0-1)
     */
    public material(double friction, double restitution, double damping){
        this.friction = friction;
        this.restitution = restitution;
        this.damping = damping;
    }

}
