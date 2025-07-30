# Mechanism Framework for FRC

This framework provides an abstraction layer for FRC robot mechanisms, allowing you to model physical structures and their relationships in a tree-like hierarchy.

## Overview

The framework consists of several key components:

### Core Classes

1. **Mechanism** - Base abstract class for all mechanisms
2. **LinearMechanism** - For linear motion structures (elevators, slides)
3. **RotatingMechanism** - For rotational motion structures (arms, turrets)
4. **MechanismSystem** - Manages multiple mechanisms and their relationships
5. **physicalProperties** - Describes mass, center of gravity, and moment of inertia

### Motor Integration

- Uses existing **KrakenIO** implementation
- **MotorConfig** for motor configuration
- **MotorInputs** for motor state feedback
- **MotorRequest** for motor control commands

## Basic Usage

### 1. Create Physical Properties

```java
SimpleMatrix cg = new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.5); // Center of gravity
SimpleMatrix moi = new SimpleMatrix(3, 3, true, 
    1.0, 0.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 0.0, 0.1); // Moment of inertia matrix
physicalProperties props = new physicalProperties(10.0, cg, moi, Optional.empty());
```

### 2. Create Mechanisms

```java
// Linear mechanism (elevator)
SimpleMatrix motionAxis = new SimpleMatrix(3, 1, true, 0.0, 0.0, 1.0); // Vertical motion
SimpleMatrix startPoint = new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.0); // Start position
LinearMechanism<configBase, inputBase, requestBase> elevator = 
    new LinearMechanism<>("Elevator", props, motionAxis, startPoint);

// Rotating mechanism (arm)
SimpleMatrix rotationAxis = new SimpleMatrix(3, 1, true, 0.0, 0.0, 1.0); // Z-axis rotation
SimpleMatrix pivotPoint = new SimpleMatrix(3, 1, true, 0.0, 0.0, 0.0); // Pivot point
RotatingMechanism<configBase, inputBase, requestBase> arm = 
    new RotatingMechanism<>("Arm", props, rotationAxis, pivotPoint);
```

### 3. Configure Motors

```java
MotorConfig config = new MotorConfig("MotorName", 1, "rio");
config.kP = 0.1;
config.kI = 0.0;
config.kD = 0.01;
config.maxVel = 5.0;
config.maxAcc = 10.0;
config.gearRatio = 10.0;
```

### 4. Register Motors with Mechanisms

```java
KrakenIO motor = new KrakenIO(config);
elevator.registerMotor(motor, config);
```

### 5. Create Mechanism System

```java
MechanismSystem system = new MechanismSystem("RobotSystem");
system.addMechanism(elevator);
system.addMechanism(arm);
system.setParentChildRelation("Elevator", "Arm"); // Arm is attached to elevator
```

### 6. Control Mechanisms

```java
// Set target position
Mechanism.SetPoint setpoint = elevator.new SetPoint(1.0, 0.5, 0.0, 0.0); // position, velocity, acceleration, feedforward
elevator.setTargetSetpoint(setpoint);

// Update and execute control (call in periodic)
system.updateAllMechanismStates();
system.executeAllMechanismControl();

// Check if at target
if (elevator.isAtTarget()) {
    System.out.println("Elevator reached target!");
}
```

## Features

### Physics-Based Feedforward

The framework automatically calculates feedforward forces/torques based on:

- Gravity compensation
- Inertia compensation
- Friction compensation
- Coriolis effects (for rotating mechanisms)

### Tree-like Structure

Mechanisms can be organized in parent-child relationships:

- Child mechanisms inherit motion from parents
- Feedforward calculations consider the entire kinematic chain
- System-level control and monitoring

### Motor Integration

- Direct integration with existing KrakenIO
- Automatic state updates from motor feedback
- Position and velocity control with tolerance checking
- Emergency stop functionality

## Example Implementation

See `MechanismExample.java` for a complete example showing how to:

- Set up an elevator and arm system
- Configure motors and mechanisms
- Implement periodic control
- Handle setpoint management

## Integration with WPILib

The framework is designed to work seamlessly with WPILib:

- Mechanisms extend `SubsystemBase`
- Use in commands and subsystems
- Periodic updates every 20ms
- Integration with WPILib's command-based architecture

## Benefits

1. **Abstraction** - Hide complex motor control behind simple mechanism interfaces
2. **Physics** - Automatic feedforward calculation based on physical properties
3. **Modularity** - Easy to add/remove mechanisms and change relationships
4. **Reusability** - Same framework works for different robot designs
5. **Maintainability** - Clear separation of concerns and well-defined interfaces
