package bgu.spl.mics.application.objects;
import java.util.List;
/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */


public class GPSIMU {
    private int currentTick; // The current time
    private STATUS status; // Status of the GPS and IMU
    private List<Pose> poseList; // List of time-stamped poses


    // Constructor
    public GPSIMU(int currentTick, STATUS status, List<Pose> poseList) {
        this.currentTick = currentTick;
        this.status = status;
        this.poseList = poseList;
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

    public Pose getPoseByTick(int tick) {
        return getPoseList().get(tick);
    }


}

