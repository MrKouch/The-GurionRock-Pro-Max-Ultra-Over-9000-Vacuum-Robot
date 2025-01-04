package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final String id; // The ID of the LiDar
    private int frequency; // The time interval at which the LiDar sends new events
    private STATUS status; // The status of the LiDar
    private List<StampedDetectedObjects> stampedDetectedObjects;
    private List<TrackedObject> waitingObjects;
    private List<TrackedObject> lastTrackedObjects; // The last objects the LiDar tracked

    // ADD FIELDS OR METHODS TO GET INFORMATION FROM THE LIDARDATABASE

    // Constructor
    public LiDarWorkerTracker(String id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.stampedDetectedObjects = new LinkedList<>();
        this.waitingObjects = new LinkedList<>();
        this.lastTrackedObjects = new LinkedList<TrackedObject>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public List<StampedDetectedObjects> getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    public List<TrackedObject> getWaitingObjects() {
        return waitingObjects;
    }

    // Setters (only for mutable fields)
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void detectedToTracked(int currentTime) {
        for (StampedDetectedObjects objects : stampedDetectedObjects) {
            if(objects.getTime() + frequency <= currentTime) {
                for (DetectedObject detectedObject : objects.getDetectedObjects()) {
                    TrackedObject trackedObject = new TrackedObject(detectedObject, objects.getTime());
                    waitingObjects.add(trackedObject);
                }
                StatisticalFolder.getInstance().incrementNumTrackedObjects(waitingObjects.size());
            }
        }
    }

    public void updateLastTrackedObjects(int currentTime) {
        StampedCloudPoints lastCloudPoints = LiDarDataBase.getInstance().getCloudPoints().get(currentTime);
        if(lastCloudPoints != null) {
            lastTrackedObjects.clear();
            List<String> IDsByDetectionTime = LiDarDataBase.getInstance().getIDsByDetectionTime().get(currentTime);
            for (String ID : IDsByDetectionTime) {
                TrackedObject trackedObject = new TrackedObject(ID, currentTime, "LidarData has no description. get it from the camera data.", lastCloudPoints.getCloudPoints());
                lastTrackedObjects.add(trackedObject);
            }
        }
    }
}
