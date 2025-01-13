package bgu.spl.mics.application.objects;
import java.util.HashMap;

import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.FrequencyBroadcast;
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
    public Camera(int id, int frequency, HashMap<Integer, StampedDetectedObjects> detectedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjects = detectedObjects;
        this.lastDetectedObjects = new StampedDetectedObjects(0, null);
        this.prevLastDetectedObjects = new StampedDetectedObjects(0, null);
        this.latestDetectionTime = computeLatestDetectionTime();
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
        if (currentTime >= frequency) {
            if (detectedObjects.get(currentTime - frequency) != null && !detectedObjects.get(currentTime - frequency).getDetectedObjects().isEmpty())
                return new StampedDetectedObjects(detectedObjects.get(currentTime - frequency));
        }
        return null;
    }

    // Updates the last detected objects and sends the data to the statistical folder
    public void updateLastDetectedObjects(int currentTime) {
        StampedDetectedObjects objects = detectedObjects.get(currentTime);
        if (objects != null) {
            if (lastDetectedObjects.getDetectedObjects() != null) {
                prevLastDetectedObjects = new StampedDetectedObjects(lastDetectedObjects);
            }
            lastDetectedObjects = objects;
            StatisticalFolder.getInstance().incrementNumDetectedObjects(objects.getDetectedObjects().size());
        }
    }
    
    public String hasErrorNow(int time) {
        StampedDetectedObjects currentObjects = detectedObjects.get(time);
        if (currentObjects != null) {
            for (DetectedObject detectedObject : currentObjects.getDetectedObjects()) {
                if (detectedObject.getId().equals("ERROR")) {
                    return detectedObject.getDescription();
                }
            }
        }
        return "NO ERROR";
    }
    
    /**
     * @PRE:
     * - `currentTime` must be a non-negative integer.
     * - `detectedObjects` must be properly initialized (not null).
     *
     * @POST:
     * - If `currentTime` exceeds `getLatestDetectionTime() + getFrequency()`:
     *   - The camera status is set to `STATUS.DOWN`.
     *   - A `TerminatedBroadcast` message is returned.
     * - If an error object is detected at the current time:
     *   - The camera status is set to `STATUS.ERROR`.
     *   - A `CrashedBroadcast` message is returned with the error description.
     * - If no error occurs:
     *   - `lastDetectedObjects` and `prevLastDetectedObjects` may be updated.
     *   - If ready detected objects are available, a `DetectObjectsEvent` is returned.
     * - If none of the above cases occur, the method returns `null`.
     */
    public Message operateTick(int currentTime) {
        // if (currentTime == 1)
        //     return new FrequencyBroadcast(frequency);
        if (currentTime > getLatestDetectionTime() + getFrequency()) {
            setStatus(STATUS.DOWN);
            return new TerminatedBroadcast(CameraService.class, getId() + " finished");
        }
        else {
            String errorDescription = hasErrorNow(currentTime);
            if (!errorDescription.equals("NO ERROR")) {
                setStatus(STATUS.ERROR);
                return new CrashedBroadcast("Camera " + getId(), errorDescription, currentTime);
            }
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

    // @PRE: none
    // @POST: trivial (simple getter)
    public int getId() {
        return id;
    }

    // @PRE: none
    // @POST: trivial (simple getter)
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

    // @PRE: none
    // @POST: trivial (simple getter)
    public HashMap<Integer, StampedDetectedObjects> getDetectedObjects() {
        return detectedObjects;
    }

    // @PRE: none
    // @POST: trivial (simple getter)
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
}