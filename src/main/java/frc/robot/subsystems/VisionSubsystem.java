package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.vision.VisionAggregator;

/**
 * Thin wrapper around {@link VisionAggregator} that exposes the current best
 * robot pose on the dashboard and provides accessors for other subsystems/commands.
 */
public class VisionSubsystem extends SubsystemBase {
  private final VisionAggregator aggregator = new VisionAggregator();

  public VisionSubsystem() {}

  public Pose3d getFieldToRobotPose() {
    return aggregator.getFieldToRobotPose();
  }

  public boolean hasFreshPose() {
    return aggregator.hasFreshPose();
  }

  @Override
  public void periodic() {
    Pose3d pose = aggregator.getFieldToRobotPose();
    if (pose != null) {
      SmartDashboard.putNumber("vision/pose/x", pose.getX());
      SmartDashboard.putNumber("vision/pose/y", pose.getY());
      SmartDashboard.putNumber("vision/pose/z", pose.getZ());
      SmartDashboard.putNumber("vision/pose/rx", pose.getRotation().getX());
      SmartDashboard.putNumber("vision/pose/ry", pose.getRotation().getY());
      SmartDashboard.putNumber("vision/pose/rz", pose.getRotation().getZ());
      SmartDashboard.putString("vision/bestDevice", String.valueOf(aggregator.getBestDeviceId()));
      SmartDashboard.putNumber("vision/bestError", aggregator.getBestError());
      SmartDashboard.putBoolean("vision/hasPose", true);
    } else {
      SmartDashboard.putBoolean("vision/hasPose", false);
    }
  }
}


