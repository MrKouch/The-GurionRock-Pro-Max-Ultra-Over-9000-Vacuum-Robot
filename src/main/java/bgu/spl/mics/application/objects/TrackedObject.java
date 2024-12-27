package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private final String id; // The ID of the object
    private final int time; // The time the object was tracked
    private String description; // Description of the object
    private List<CloudPoint> coordinates; // The coordinates of the object

    // Constructor
    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    // Setters (only for mutable fields)
    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = coordinates;
    }
}
