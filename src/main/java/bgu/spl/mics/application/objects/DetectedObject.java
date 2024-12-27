package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
    private final String id; // The ID of the object
    private String description; // Description of the object

    // Constructor
    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
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
}

