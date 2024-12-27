package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    // Fields
    private int systemRuntime; // Total runtime of the system, measured in ticks
    private int numDetectedObjects; // Cumulative count of objects detected by all cameras
    private int numTrackedObjects; // Cumulative count of objects tracked by all LiDAR workers
    private int numLandmarks; // Total number of unique landmarks identified and mapped

    // Constructor
    public StatisticalFolder(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks) {
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = numDetectedObjects;
        this.numTrackedObjects = numTrackedObjects;
        this.numLandmarks = numLandmarks;
    }

    // Default Constructor
    public StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    // Getters
    public int getSystemRuntime() {
        return systemRuntime;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    public int getNumLandmarks() {
        return numLandmarks;
    }

    // Setters
    public void setSystemRuntime(int systemRuntime) {
        this.systemRuntime = systemRuntime;
    }

    public void setNumDetectedObjects(int numDetectedObjects) {
        this.numDetectedObjects = numDetectedObjects;
    }

    public void setNumTrackedObjects(int numTrackedObjects) {
        this.numTrackedObjects = numTrackedObjects;
    }

    public void setNumLandmarks(int numLandmarks) {
        this.numLandmarks = numLandmarks;
    }

    // Methods to update statistics incrementally
    public void incrementSystemRuntime(int ticks) {
        this.systemRuntime += ticks;
    }

    public void incrementNumDetectedObjects(int count) {
        this.numDetectedObjects += count;
    }

    public void incrementNumTrackedObjects(int count) {
        this.numTrackedObjects += count;
    }

    public void incrementNumLandmarks(int count) {
        this.numLandmarks += count;
    }

    @Override
    public String toString() {
        return "StatisticalFolder{" +
                "systemRuntime=" + systemRuntime +
                ", numDetectedObjects=" + numDetectedObjects +
                ", numTrackedObjects=" + numTrackedObjects +
                ", numLandmarks=" + numLandmarks +
                '}';
    }
}

