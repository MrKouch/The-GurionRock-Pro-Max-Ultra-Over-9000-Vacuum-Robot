package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.services.LiDarService;

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
    private List<TrackedObject> lastTrackedObjects;
    private List<TrackedObject> prevLastTrackedObjects;
    private boolean isFaulty;

    // Constructor
    public LiDarWorkerTracker(String id, int frequency, List<StampedDetectedObjects> stampedDetectedObjects, boolean isFaulty) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.stampedDetectedObjects = stampedDetectedObjects;
        this.waitingObjects = new LinkedList<TrackedObject>();
        this.latestDetectionTime = computeLatestDetectionTime();
        this.lastTrackedObjects = new LinkedList<TrackedObject>();
        this.prevLastTrackedObjects = new LinkedList<TrackedObject>();
        this.isFaulty = isFaulty;
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

    public List<StampedDetectedObjects> getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    public List<TrackedObject> getWaitingObjects() {
        return waitingObjects;
    }

    public int getLastDetectionTime() {
        return this.lastTrackedObjects.get(0).getTime();
    }

    public void setLastTrackedObjectsToPrev() {
        this.lastTrackedObjects = prevLastTrackedObjects;
    }

    // Setters (only for mutable fields)
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public boolean isFaulty() {
        return isFaulty;
    }

    public void detectedToTracked(int currentTime) {
        for (StampedDetectedObjects objects : stampedDetectedObjects) {
            if(objects.getTime() + frequency <= currentTime) {
                for (DetectedObject detectedObject : objects.getDetectedObjects()) {
                    for (TrackedObject trackedObject : LiDarDataBase.getInstance().getTrackedObjects()) {
                        if(trackedObject.getId() == detectedObject.getId()) {
                            trackedObject.setDescription(detectedObject.getDescription());
                            waitingObjects.add(trackedObject);
                        }
                    }
                }
            }
        }
    }

    public boolean hasErrorNow(int time) {
        for (StampedDetectedObjects objects : stampedDetectedObjects) {
            if(objects.getTime() == time) {
                for (TrackedObject trackedObject : LiDarDataBase.getInstance().getTrackedObjects()) {
                    if(trackedObject.getId() == "ERROR") {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateStatistics(int currentTime) {
        int newTracks = 0;
        List<TrackedObject> latestObjects = new LinkedList<>();
        for (TrackedObject object : LiDarDataBase.getInstance().getTrackedObjects()) {
            if(object.getTime() == currentTime) {
                latestObjects.add(object);
                newTracks++;
            }
        }
        if (newTracks > 0) {
            this.prevLastTrackedObjects = new LinkedList<>(this.lastTrackedObjects);
            this.lastTrackedObjects = latestObjects;
            StatisticalFolder.getInstance().incrementNumTrackedObjects(newTracks);
        }
    }

    public Message operateTick(int currentTime, String senderId) {
        if (currentTime > getLatestDetectionTime() + getFrequency()) {
            return new TerminatedBroadcast(LiDarService.class, getId() + " finished");
        }
        else {
            if (hasErrorNow(currentTime))
                return new CrashedBroadcast("liDarWorkerTracker" + getId(), "liDarWorkerTracker" + getId() + " crashed", currentTime);
            else {
                updateStatistics(currentTime);
                detectedToTracked(currentTime);
                // Transfer the latest tracked objects data to the fusionSLAM using the message bus
                if (waitingObjects.size() > 0)
                    return new TrackedObjectsEvent(senderId, getWaitingObjects());
            }
        }
        return null;
    }
}
