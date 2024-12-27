package bgu.spl.mics.application.objects;
import java.util.List;
/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */


public class LandMark {
    private final String id; // The ID of the landmark
    private String description; // Description of the landmark
    private List<CloudPoint> coordinates; // List of coordinates

    // Constructor
    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = coordinates;
    }
}

