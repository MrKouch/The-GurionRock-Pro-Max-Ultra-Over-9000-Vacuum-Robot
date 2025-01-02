package bgu.spl.mics.application.objects;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private List<LandMark> landmarks; // Should be implemented as an array
    private List<Pose> poses; // List of previous poses needed for calculations
    
    private int activeSensors;
    private String crashedSensorId;
    private String errorDescription;
    
    // Constructor
    private FusionSlam() {
        this.landmarks = landmarks;
        this.poses = poses;
        // todo: initialize activeSensors
        this.crashedSensorId = "";
        this.errorDescription = "";
    }

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
    // Getters and Setters
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<LandMark> landmarks) {
        this.landmarks = landmarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }

    public String getCrashedSensorId() {
        return crashedSensorId;
    }

    public void setCrashedSensorId(String crashedSensorId) {
        this.crashedSensorId = crashedSensorId;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

}
