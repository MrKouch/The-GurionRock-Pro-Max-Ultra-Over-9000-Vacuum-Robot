package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    // Fields
    private int systemRuntime; // Total runtime of the system, measured in ticks
    private AtomicInteger numDetectedObjects; // Cumulative count of objects detected by all cameras
    private AtomicInteger numTrackedObjects; // Cumulative count of objects tracked by all LiDAR workers
    private int numLandmarks; // Total number of unique landmarks identified and mapped

    // Constructor
    private StatisticalFolder(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks) {
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = new AtomicInteger(numDetectedObjects);
        this.numTrackedObjects = new AtomicInteger(numTrackedObjects);
        this.numLandmarks = numLandmarks;
    }

    // Default Constructor
    private StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = 0;
    }

    private static class StatisticalFolderHolder {
        private static StatisticalFolder instance = new StatisticalFolder();
    }

    public static StatisticalFolder getInstance() {
        return StatisticalFolderHolder.instance;
    }

    // Getters
    public int getSystemRuntime() {
        return systemRuntime;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks;
    }

    // Methods to update statistics incrementally
    public void incrementSystemRuntime() {
        this.systemRuntime++;
    }

    public void incrementNumDetectedObjects(int count) {
        int oldval;
        int newval;
        do {
            oldval = numDetectedObjects.get();
            newval = oldval + count;
        } while(!numDetectedObjects.compareAndSet(oldval, newval));
    }

    public void incrementNumTrackedObjects(int count) {
        int oldval;
        int newval;
        do {
            oldval = numTrackedObjects.get();
            newval = oldval + count;
        } while(!numTrackedObjects.compareAndSet(oldval, newval));
    }

    public void incrementNumLandmarks() {
        this.numLandmarks++;
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

