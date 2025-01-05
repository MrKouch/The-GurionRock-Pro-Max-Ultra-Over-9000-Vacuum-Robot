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
    private int latestDetectionTime;

    //NO NEED?
    //private List<TrackedObject> lastTrackedObjects; // The last objects the LiDar tracked

    // ADD FIELDS OR METHODS TO GET INFORMATION FROM THE LIDARDATABASE

    // Constructor
    public LiDarWorkerTracker(String id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.stampedDetectedObjects = new LinkedList<>();
        this.waitingObjects = new LinkedList<>();
        this.latestDetectionTime = computeLatestDetectionTime();
        //NO NEED?
        //this.lastTrackedObjects = new LinkedList<TrackedObject>();
    }

    private int computeLatestDetectionTime() {
        int maxTime = 0;
        for (TrackedObject detectedObject : LiDarDataBase.getInstance().getTrackedObjects()) { 
            if (detectedObject.getTime() > maxTime)
                maxTime = detectedObject.getTime();
        }
        return maxTime;
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

    public int getLatestDetectionTime() {
        return latestDetectionTime;
    }

    // public List<TrackedObject> getLastTrackedObjects() {
    //     return lastTrackedObjects;
    // }

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
                    for (TrackedObject trackedObject : LiDarDataBase.getInstance().getTrackedObjects()) {
                        if(trackedObject.getId() == detectedObject.getId()) {
                            waitingObjects.add(trackedObject);
                        }
                    }
                }
            }
        }
    }

    public void updateStatistics(int currentTime) {
        int newTracks = 0;
        for (TrackedObject object : LiDarDataBase.getInstance().getTrackedObjects()) {
            if(object.getTime() == currentTime) {
                newTracks++;
            }
        }
        StatisticalFolder.getInstance().incrementNumTrackedObjects(newTracks);
    }
}
