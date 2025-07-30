package frc.lib.structure.mechanisms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.ejml.simple.SimpleMatrix;
import frc.lib.structure.configBase;
import frc.lib.structure.inputBase;
import frc.lib.structure.requestBase;

/**
 * Mechanism system manager Manages physical relationships between multiple mechanisms, builds tree
 * structure Calculates feedforward forces for the entire system considering parent-child
 * interactions
 */
public class MechanismSystem {

    // Mechanism mapping: name -> mechanism object
    private Map<String, Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase>> mechanisms;

    // Parent-child relationship mapping: child mechanism -> parent mechanism
    private Map<String, String> parentChildRelations;

    // Child mechanisms mapping: parent mechanism -> list of children
    private Map<String, List<String>> childrenMap;

    // System root node
    private String rootMechanism;

    // System name
    private String systemName;

    public MechanismSystem(String systemName) {
        this.systemName = systemName;
        this.mechanisms = new ConcurrentHashMap<>();
        this.parentChildRelations = new ConcurrentHashMap<>();
        this.childrenMap = new ConcurrentHashMap<>();
        this.rootMechanism = null;
    }

    /**
     * Add mechanism to system
     */
    public void addMechanism(
            Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism) {
        mechanisms.put(mechanism.getName(), mechanism);

        // If it's the first mechanism, set as root node
        if (rootMechanism == null) {
            rootMechanism = mechanism.getName();
        }
    }

    /**
     * Set parent-child relationship
     */
    public void setParentChildRelation(String parentName, String childName) {
        if (!mechanisms.containsKey(parentName) || !mechanisms.containsKey(childName)) {
            throw new IllegalArgumentException("Parent or child mechanism not found");
        }

        parentChildRelations.put(childName, parentName);

        // Update children map
        childrenMap.computeIfAbsent(parentName, k -> new ArrayList<>()).add(childName);
    }

    /**
     * Get mechanism
     */
    public Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> getMechanism(
            String name) {
        return mechanisms.get(name);
    }

    /**
     * Get all mechanisms
     */
    public Map<String, Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase>> getAllMechanisms() {
        return new HashMap<>(mechanisms);
    }

    /**
     * Get child mechanisms
     */
    public List<String> getChildren(String parentName) {
        return childrenMap.getOrDefault(parentName, new ArrayList<>());
    }

    /**
     * Get parent mechanism
     */
    public String getParent(String childName) {
        return parentChildRelations.get(childName);
    }

    /**
     * Update all mechanism states from motor feedback
     */
    public void updateAllMechanismStates() {
        for (Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism : mechanisms
                .values()) {
            mechanism.updateMechanismState();
        }
    }

    /**
     * Execute control for all mechanisms
     */
    public void executeAllMechanismControl() {
        for (Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism : mechanisms
                .values()) {
            mechanism.executeControl();
        }
    }

    /**
     * Set setpoints for multiple mechanisms
     */
    public void setMechanismSetpoints(Map<String, SetPoint> setpoints) {
        for (Map.Entry<String, SetPoint> entry : setpoints.entrySet()) {
            Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism =
                    mechanisms.get(entry.getKey());
            if (mechanism != null) {
                mechanism.setTargetSetpoint(entry.getValue());
            }
        }
    }

    /**
     * Check if all mechanisms are at target
     */
    public boolean areAllMechanismsAtTarget() {
        for (Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism : mechanisms
                .values()) {
            if (!mechanism.isAtTarget()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Emergency stop all mechanisms
     */
    public void emergencyStopAllMechanisms() {
        for (Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism : mechanisms
                .values()) {
            mechanism.emergencyStop();
        }
    }

    /**
     * Calculate feedforward forces for the entire system considering parent-child interactions This
     * method implements true tree-based feedforward calculation
     */
    public Map<String, SimpleMatrix> calculateSystemFeedforward(Map<String, SetPoint> setpoints) {
        Map<String, SimpleMatrix> feedforwardForces = new HashMap<>();
        Map<String, MechanismState> mechanismStates = new HashMap<>();

        // First pass: Update all mechanism states based on setpoints
        updateAllMechanismStatesFromSetpoints(setpoints, mechanismStates);

        // Second pass: Calculate feedforward from root to leaves (top-down)
        calculateTopDownFeedforward(rootMechanism, setpoints, feedforwardForces, mechanismStates,
                new SimpleMatrix(3, 1));

        // Third pass: Calculate reaction forces from leaves to root (bottom-up)
        calculateBottomUpReactionForces(rootMechanism, feedforwardForces, mechanismStates);

        return feedforwardForces;
    }

    /**
     * Update all mechanism states based on setpoints
     */
    private void updateAllMechanismStatesFromSetpoints(Map<String, SetPoint> setpoints,
            Map<String, MechanismState> mechanismStates) {
        for (Map.Entry<String, SetPoint> entry : setpoints.entrySet()) {
            String mechanismName = entry.getKey();
            SetPoint setpoint = entry.getValue();
            Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism =
                    mechanisms.get(mechanismName);

            if (mechanism != null) {
                MechanismState state = new MechanismState();
                state.mechanism = mechanism;
                state.setpoint = setpoint;

                // Update mechanism state based on type
                if (mechanism instanceof LinearMechanism) {
                    LinearMechanism<?, ?, ?> linearMech = (LinearMechanism<?, ?, ?>) mechanism;
                    linearMech.setCurrentPosition(setpoint.position);
                    linearMech.setVelocity(setpoint.velocity);
                    linearMech.setAcceleration(setpoint.acceleration);

                    state.position = setpoint.position;
                    state.velocity = setpoint.velocity;
                    state.acceleration = setpoint.acceleration;
                    state.type = MechanismType.LINEAR;
                } else if (mechanism instanceof RotatingMechanism) {
                    RotatingMechanism<?, ?, ?> rotatingMech =
                            (RotatingMechanism<?, ?, ?>) mechanism;
                    rotatingMech.setCurrentAngle(setpoint.position);
                    rotatingMech.setAngularVelocity(setpoint.velocity);
                    rotatingMech.setAngularAcceleration(setpoint.acceleration);

                    state.position = setpoint.position;
                    state.velocity = setpoint.velocity;
                    state.acceleration = setpoint.acceleration;
                    state.type = MechanismType.ROTATING;
                }

                mechanismStates.put(mechanismName, state);
            }
        }
    }

    /**
     * Top-down feedforward calculation (parent motion affects children)
     */
    private void calculateTopDownFeedforward(String mechanismName, Map<String, SetPoint> setpoints,
            Map<String, SimpleMatrix> feedforwardForces,
            Map<String, MechanismState> mechanismStates, SimpleMatrix parentMotion) {

        Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism =
                mechanisms.get(mechanismName);

        if (mechanism == null) {
            return;
        }

        MechanismState state = mechanismStates.get(mechanismName);
        if (state == null) {
            return;
        }

        // Calculate base feedforward for this mechanism
        SimpleMatrix baseFeedforward = mechanism.getFeedforward(parentMotion);

        // Add parent motion effects to children
        List<String> children = getChildren(mechanismName);
        for (String childName : children) {
            MechanismState childState = mechanismStates.get(childName);
            if (childState != null) {
                // Calculate how parent motion affects child
                SimpleMatrix childMotionEffect =
                        calculateChildMotionEffect(state, childState, parentMotion);

                // Recursively calculate child feedforward with combined motion
                SimpleMatrix combinedMotion = parentMotion.plus(childMotionEffect);
                calculateTopDownFeedforward(childName, setpoints, feedforwardForces,
                        mechanismStates, combinedMotion);
            }
        }

        // Store the base feedforward (will be modified in bottom-up pass)
        feedforwardForces.put(mechanismName, baseFeedforward);
    }

    /**
     * Bottom-up reaction force calculation (children affect parent)
     */
    private SimpleMatrix calculateBottomUpReactionForces(String mechanismName,
            Map<String, SimpleMatrix> feedforwardForces,
            Map<String, MechanismState> mechanismStates) {

        Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism =
                mechanisms.get(mechanismName);

        if (mechanism == null) {
            return new SimpleMatrix(3, 1);
        }

        MechanismState state = mechanismStates.get(mechanismName);
        if (state == null) {
            return new SimpleMatrix(3, 1);
        }

        // Get base feedforward for this mechanism
        SimpleMatrix totalFeedforward =
                feedforwardForces.getOrDefault(mechanismName, new SimpleMatrix(3, 1));

        // Calculate reaction forces from all children
        List<String> children = getChildren(mechanismName);
        SimpleMatrix totalReactionForce = new SimpleMatrix(3, 1);

        for (String childName : children) {
            MechanismState childState = mechanismStates.get(childName);
            if (childState != null) {
                // Recursively get child's total force (including its children's reactions)
                SimpleMatrix childTotalForce = calculateBottomUpReactionForces(childName,
                        feedforwardForces, mechanismStates);

                // Calculate reaction force that child exerts on parent
                SimpleMatrix reactionForce =
                        calculateReactionForce(state, childState, childTotalForce);
                totalReactionForce = totalReactionForce.plus(reactionForce);
            }
        }

        // Add reaction forces to this mechanism's feedforward
        totalFeedforward = totalFeedforward.plus(totalReactionForce);
        feedforwardForces.put(mechanismName, totalFeedforward);

        return totalFeedforward;
    }

    /**
     * Calculate how parent motion affects child motion
     */
    private SimpleMatrix calculateChildMotionEffect(MechanismState parentState,
            MechanismState childState, SimpleMatrix parentMotion) {

        // This is a simplified calculation - in reality, this would involve
        // proper kinematic transformations based on the physical connection

        if (parentState.type == MechanismType.LINEAR && childState.type == MechanismType.LINEAR) {
            // Linear parent affects linear child through direct coupling
            return parentMotion.scale(0.5); // Simplified coupling factor
        } else if (parentState.type == MechanismType.LINEAR
                && childState.type == MechanismType.ROTATING) {
            // Linear parent affects rotating child (e.g., elevator affects arm)
            // Convert linear motion to angular motion effect
            return new SimpleMatrix(3, 1, true, parentMotion.get(0, 0) * 0.1, // Simplified coupling
                    parentMotion.get(1, 0) * 0.1, parentMotion.get(2, 0) * 0.1);
        } else if (parentState.type == MechanismType.ROTATING
                && childState.type == MechanismType.LINEAR) {
            // Rotating parent affects linear child
            return new SimpleMatrix(3, 1, true, parentMotion.get(0, 0) * 0.1,
                    parentMotion.get(1, 0) * 0.1, parentMotion.get(2, 0) * 0.1);
        } else {
            // Rotating parent affects rotating child
            return parentMotion.scale(0.3); // Simplified coupling factor
        }
    }

    /**
     * Calculate reaction force that child exerts on parent
     */
    private SimpleMatrix calculateReactionForce(MechanismState parentState,
            MechanismState childState, SimpleMatrix childForce) {

        // This calculates the reaction force that a child mechanism exerts on its parent
        // based on Newton's third law and the physical connection geometry

        if (parentState.type == MechanismType.LINEAR && childState.type == MechanismType.LINEAR) {
            // Direct force transmission
            return childForce.scale(-1.0); // Reaction force is opposite
        } else if (parentState.type == MechanismType.LINEAR
                && childState.type == MechanismType.ROTATING) {
            // Rotating child exerts torque on linear parent
            // Convert torque to force effect
            return new SimpleMatrix(3, 1, true, -childForce.get(0, 0) * 0.1,
                    -childForce.get(1, 0) * 0.1, -childForce.get(2, 0) * 0.1);
        } else if (parentState.type == MechanismType.ROTATING
                && childState.type == MechanismType.LINEAR) {
            // Linear child exerts force on rotating parent
            // Convert force to torque effect
            return new SimpleMatrix(3, 1, true, -childForce.get(0, 0) * 0.1,
                    -childForce.get(1, 0) * 0.1, -childForce.get(2, 0) * 0.1);
        } else {
            // Rotating child exerts torque on rotating parent
            return childForce.scale(-0.5); // Simplified reaction factor
        }
    }

    /**
     * Get system name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Get root mechanism name
     */
    public String getRootMechanism() {
        return rootMechanism;
    }

    /**
     * Helper class to store mechanism state information
     */
    private static class MechanismState {
        Mechanism<? extends configBase, ? extends inputBase, ? extends requestBase> mechanism;
        SetPoint setpoint;
        double position;
        double velocity;
        double acceleration;
        MechanismType type;
    }

    /**
     * Mechanism type enumeration
     */
    private enum MechanismType {
        LINEAR, ROTATING
    }
}
