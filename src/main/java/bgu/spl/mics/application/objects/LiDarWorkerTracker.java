package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final String id; // The ID of the LiDar
    private int frequency; // The time interval at which the LiDar sends new events
    private Status status; // The status of the LiDar
    private List<TrackedObject> lastTrackedObjects; // The last objects the LiDar tracked

    // ADD FIELDS OR METHODS TO GET INFORMATION FROM THE LIDARDATABASE

    public enum Status {
        UP, DOWN, ERROR
    }

    // Constructor
    public LiDarWorkerTracker(String id, int frequency, Status status, List<TrackedObject> lastTrackedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.lastTrackedObjects = lastTrackedObjects;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public Status getStatus() {
        return status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    // Setters (only for mutable fields)
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setLastTrackedObjects(List<TrackedObject> lastTrackedObjects) {
        this.lastTrackedObjects = lastTrackedObjects;
    } 
}
