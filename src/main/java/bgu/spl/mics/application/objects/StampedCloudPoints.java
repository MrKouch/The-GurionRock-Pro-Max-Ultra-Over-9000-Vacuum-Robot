package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */

public class StampedCloudPoints {
    private final String id; // The ID of the object
    private final int time; // The time the object was tracked
    private List<List<Double>> cloudPoints; // List of cloud points

    // Constructor
    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public List<List<Double>> getCloudPoints() {
        return cloudPoints;
    }

    // Setter for cloudPoints
    public void setCloudPoints(List<List<Double>> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }
}
