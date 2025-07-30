package frc.lib.structure.physics;

import org.ejml.simple.SimpleMatrix;

import frc.lib.structure.configBase;
import frc.lib.structure.device;

public  interface physicalObject<TConfig extends configBase> extends device<TConfig>{

    default physicalProperties getPhysicalProperties(){return null;}

    default SimpleMatrix getFeedforward(SimpleMatrix noninertialFrame){return new SimpleMatrix(new double[]{0, 0, 0});}
} 