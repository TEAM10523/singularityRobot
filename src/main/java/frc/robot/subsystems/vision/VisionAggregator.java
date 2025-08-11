package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.networktables.DoubleArraySubscriber;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Subsystem that aggregates AprilTag-based robot poses published by multiple
 * Singularity Vision processes over NetworkTables.
 *
 * Each vision process publishes under the root table "singularity-vision/<device_id>"
 * with the following entries:
 * - multi_pose: double[6] = {x, y, z, rx, ry, rz} (field-to-robot)
 * - multi_error: double = smaller is better (e.g. 1/area)
 *
 * This subsystem discovers all subtables dynamically, subscribes to those topics,
 * and selects the best pose (minimum positive error) each cycle. If no valid pose
 * is available, the last valid pose is retained and a flag is exposed.
 */
public class VisionAggregator extends SubsystemBase {
  private static final String ROOT_TABLE = "singularity-vision";
  private static final String KEY_MULTI_POSE = "multi_pose";
  private static final String KEY_MULTI_ERROR = "multi_error";

  private static class CameraStream {
    final DoubleArraySubscriber poseSub;
    final DoubleSubscriber errorSub;

    CameraStream(DoubleArraySubscriber poseSub, DoubleSubscriber errorSub) {
      this.poseSub = poseSub;
      this.errorSub = errorSub;
    }
  }

  private final NetworkTableInstance nt = NetworkTableInstance.getDefault();
  private final NetworkTable visionRoot = nt.getTable(ROOT_TABLE);

  private final Map<String, CameraStream> streamsByDevice = new HashMap<>();

  private Pose3d latestBestPose = null;
  private double latestBestError = Double.POSITIVE_INFINITY;
  private String latestBestDevice = null;
  private boolean hasFreshPose = false;

  public VisionAggregator() {
    discoverAndSubscribe();
  }

  /** Returns the last computed best field-to-robot pose (or null if none yet). */
  public synchronized Pose3d getFieldToRobotPose() {
    return latestBestPose;
  }

  /** Returns the device id that produced the last best pose (or null). */
  public synchronized String getBestDeviceId() {
    return latestBestDevice;
  }

  /** Returns the error associated with the last best pose (or +INF if none). */
  public synchronized double getBestError() {
    return latestBestError;
  }

  /** Whether a fresh valid pose was observed during the last periodic cycle. */
  public synchronized boolean hasFreshPose() {
    return hasFreshPose;
  }

  @Override
  public void periodic() {
    // Ensure we are subscribed to any newly appearing devices
    discoverAndSubscribe();

    // Compute best pose among current streams
    double bestErr = Double.POSITIVE_INFINITY;
    Pose3d bestPose = null;
    String bestDev = null;

    for (Map.Entry<String, CameraStream> entry : snapshot(streamsByDevice).entrySet()) {
      String deviceId = entry.getKey();
      CameraStream stream = entry.getValue();

      double[] poseArr = stream.poseSub.get();
      double err = stream.errorSub.get(Double.POSITIVE_INFINITY);

      if (!isPoseValid(poseArr) || !(err > 0.0) || Double.isNaN(err)) {
        continue;
      }

      if (err < bestErr) {
        bestErr = err;
        bestPose = toPose3d(poseArr);
        bestDev = deviceId;
      }
    }

    synchronized (this) {
      hasFreshPose = bestPose != null;
      if (bestPose != null) {
        latestBestPose = bestPose;
        latestBestError = bestErr;
        latestBestDevice = bestDev;
      }
    }
  }

  private static boolean isPoseValid(double[] pose) {
    if (pose == null || pose.length < 6) return false;
    // Python side uses -9999 sentinel for invalids
    for (int i = 0; i < 6; i++) {
      if (Double.isNaN(pose[i]) || pose[i] == -9999.0) return false;
    }
    return true;
  }

  private static Pose3d toPose3d(double[] p) {
    Objects.requireNonNull(p);
    return new Pose3d(p[0], p[1], p[2], new Rotation3d(p[3], p[4], p[5]));
  }

  private void discoverAndSubscribe() {
    for (String deviceId : visionRoot.getSubTables()) {
      if (streamsByDevice.containsKey(deviceId)) continue;

      NetworkTable dev = visionRoot.getSubTable(deviceId);
      DoubleArraySubscriber poseSub = dev.getDoubleArrayTopic(KEY_MULTI_POSE).subscribe(new double[0]);
      DoubleSubscriber errorSub = dev.getDoubleTopic(KEY_MULTI_ERROR).subscribe(Double.POSITIVE_INFINITY);
      streamsByDevice.put(deviceId, new CameraStream(poseSub, errorSub));
    }
  }

  private static <K, V> Map<K, V> snapshot(Map<K, V> map) {
    if (map.isEmpty()) return Collections.emptyMap();
    return new HashMap<>(map);
  }
}


