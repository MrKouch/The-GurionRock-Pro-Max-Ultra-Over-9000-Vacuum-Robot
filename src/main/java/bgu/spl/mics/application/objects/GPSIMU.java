package bgu.spl.mics.application.objects;
import java.util.LinkedList;
import java.util.List;
/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */


public class GPSIMU {
    private int currentTick; // The current time
    private STATUS status; // Status of the GPS and IMU
    private List<Pose> poseList; // List of time-stamped poses
    private int latestDetectionTime;

    // Constructor
    public GPSIMU(int currentTick, List<Pose> poseList) {
        this.currentTick = currentTick;
        this.status = STATUS.UP;
        this.poseList = poseList;
        this.latestDetectionTime = computeLatestDetectionTime();
    }

    // public GPSIMU() {
    //     this.currentTick = 0;
    //     this.status = STATUS.UP;
    //     this.poseList = new LinkedList<>();
    //     this.latestDetectionTime = 0;
    // }

    private int computeLatestDetectionTime() {
        int maxTime = 0;
        for (Pose pose : poseList) { 
            if (pose.getTime() > maxTime)
                maxTime = pose.getTime();
        } 
        return maxTime;
    }

    // Getters and Setters
    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    public void setPoseList(List<Pose> poses) {
        this.poseList = poses;
    }

    public Pose getPoseByTick(int tick) {
        return getPoseList().get(tick - 1);
    }

    public int getLatestDetectionTime() {
        return latestDetectionTime;
    }

}

