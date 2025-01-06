package bgu.spl.mics.application.objects;
import java.util.HashMap;
/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {

    // Fields
    private final int id; // The ID of the camera
    private int frequency; // Time interval at which the camera sends new events
    private STATUS status; // The status of the camera (Up, Down, Error)
    private HashMap<Integer, StampedDetectedObjects> detectedObjects; // Time-stamped objects detected by the camera, will be initialized in the main program
    private int latestDetectionTime; // latest time of detection of the camera
    private boolean isFaulty; // is there an error object in the data
    private int earliestErrorTime;  // time of first error


    // Constructor
    public Camera(int id, int frequency, STATUS status, HashMap<Integer, StampedDetectedObjects> detectedObjects, boolean isFaulty, int earliestErrorTime) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjects = detectedObjects;
        this.latestDetectionTime = computeLatestDetectionTime();
        this.isFaulty = isFaulty;
        this.earliestErrorTime = earliestErrorTime;
    }

    private int computeLatestDetectionTime() {
        int maxTime = 0;
        for (int time : detectedObjects.keySet()) {
            if (time > maxTime)
                maxTime = time;
        }
        return maxTime;
    }

    public StampedDetectedObjects getReadyDetectedObjects(int currentTime) {
        return detectedObjects.get(currentTime - frequency);
    }

    // Updates the last detected objects and sends the data to the statistical folder
    public void updateLastDetectedObjects(int currentTime) {
        StampedDetectedObjects objects = detectedObjects.get(currentTime);
        if(objects != null) {
            // NO NEED?
            // lastDetectedObjects = objects;
            StatisticalFolder.getInstance().incrementNumDetectedObjects(objects.getDetectedObjects().size());
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public HashMap<Integer, StampedDetectedObjects> getDetectedObjects() {
        return detectedObjects;
    }

    public int getLatestDetectionTime() {
        return latestDetectionTime;
    }

    public int getEarliestErrorTime() {
        return earliestErrorTime;
    }

    public boolean isFaulty() {
        return isFaulty;
    }

}