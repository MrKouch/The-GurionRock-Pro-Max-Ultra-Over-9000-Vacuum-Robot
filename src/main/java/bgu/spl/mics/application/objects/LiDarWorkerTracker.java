package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.Track;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final String id; // The ID of the LiDar
    private int frequency; // The time interval at which the LiDar sends new events
    private STATUS status; // The status of the LiDar
    private List<TrackedObject> lastTrackedObjects; // The last objects the LiDar tracked

    // ADD FIELDS OR METHODS TO GET INFORMATION FROM THE LIDARDATABASE

    // Constructor
    public LiDarWorkerTracker(String id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
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

    // Setters (only for mutable fields)
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void findTrackedObjects(int detectionTime) {
        for (StampedCloudPoints stampedCloudPoints : LiDarDataBase.getInstance("./lidar_data.json").getCloudPoints()) { // not sure about the input
            for(TrackedObject object : lastTrackedObjects) {
                if(object.getTime() == stampedCloudPoints.getTime() && object.getId() == stampedCloudPoints.getId()) {
                    object.getCoordinates().add(stampedCloudPoints.getCloudPoints());
                }
            }
            
            // if(stampedCloudPoints.getTime() == detectionTime) {
            //     for(TrackedObject object : lastTrackedObjects) {
            //         for(stampedCloudPoints )
            //         if(stampedCloudPoints.getId() == lastTrackedObjects.)
            //     }
            // }
        }
    }
    
    // public void addTrackedObject(DetectedObject detectedObject) {

    // }
}
