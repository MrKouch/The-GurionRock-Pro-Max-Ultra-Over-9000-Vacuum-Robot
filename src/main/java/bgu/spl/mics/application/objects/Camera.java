package bgu.spl.mics.application.objects;
import java.util.HashMap;

import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.services.CameraService;
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
    private StampedDetectedObjects lastDetectedObjects;
    private StampedDetectedObjects prevLastDetectedObjects;
    private int latestDetectionTime; // latest time of detection of the camera
    private boolean isFaulty; // is there an error object in the data


    // Constructor
    public Camera(int id, int frequency, STATUS status, HashMap<Integer, StampedDetectedObjects> detectedObjects, boolean isFaulty) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjects = detectedObjects;
        this.lastDetectedObjects = new StampedDetectedObjects(0, null);
        this.prevLastDetectedObjects = new StampedDetectedObjects(0, null);
        this.latestDetectionTime = computeLatestDetectionTime();
        this.isFaulty = isFaulty;
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
        return currentTime >= frequency ? detectedObjects.get(currentTime - frequency) : null;
    }

    // Updates the last detected objects and sends the data to the statistical folder
    public void updateLastDetectedObjects(int currentTime) {
        StampedDetectedObjects objects = detectedObjects.get(currentTime);
        if (objects != null) {
            prevLastDetectedObjects = new StampedDetectedObjects(lastDetectedObjects);
            lastDetectedObjects = objects;
            StatisticalFolder.getInstance().incrementNumDetectedObjects(objects.getDetectedObjects().size());
        }
    }

    public boolean hasErrorNow(int time) {
        StampedDetectedObjects currentObjects = detectedObjects.get(time);
        for (DetectedObject detectedObject : currentObjects.getDetectedObjects()) {
            if (detectedObject.getId() == "ERROR")
                return true;
        }
        return false;
    }

    public Message operateTick(int currentTime) {
        if (currentTime > getLatestDetectionTime() + getFrequency()) {
            return new TerminatedBroadcast(CameraService.class, getId() + " finished");
        }
        else {
            if (hasErrorNow(currentTime))
                return new CrashedBroadcast("camera" + getId(), "camera" + getId() + " crashed", currentTime);
            else {
                updateLastDetectedObjects(currentTime);
                StampedDetectedObjects readyDetectedObjects = getReadyDetectedObjects(currentTime);
                if (readyDetectedObjects != null) {
                    return new DetectObjectsEvent(readyDetectedObjects);
                }
            }
        }
        return null;
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

    public StampedDetectedObjects getLastDetectedObjects() {
        return lastDetectedObjects;
    }

    public void setLastDetectedObjectsToPrev() {
        this.lastDetectedObjects = prevLastDetectedObjects;
    }

    public boolean getIsFaulty() {
        return isFaulty;
    }
    

    // NO NEED?
    // public StampedDetectedObjects getLastDetectedObjects() {
    //     return lastDetectedObjects;
    // }

}