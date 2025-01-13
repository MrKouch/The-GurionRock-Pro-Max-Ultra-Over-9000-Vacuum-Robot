package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.management.RuntimeErrorException;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private List<LandMark> landmarks;
    private List<Pose> poses;
    private List<TrackedObject> waitingTrackedObjects;
    private int activeSensors;
    private String crashedSensorId;
    private String errorDescription;

    // Constructor
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.waitingTrackedObjects = new LinkedList<>();
        this.activeSensors = 0;
        this.crashedSensorId = "";
        this.errorDescription = "";
    }
    

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
    
    // @PRE: none
    // @POST: trivial (simple getter)
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public LandMark getLandmark(String id) {
        for (LandMark landmark : landmarks) {
            if (landmark.getId().equals(id)) {
                return landmark;
            }
        }
        return null;
    }

    public void setLandmarks(List<LandMark> landmarks) {
        this.landmarks = landmarks;
    }

    public List<Pose> getposes() {
        return poses;
    }

    // @PRE: none
    // @POST: trivial (simple getter)
    public Pose getPose(int time) {
        if (time > 0) {
            if (poses.size() > time - 1)
                return poses.get(time - 1);
        }
        System.out.println("asked for pose at time 0, or pose that hasn't been created yet");
        return null;
    }

    public void addPose(int time, Pose newPose) {
        this.poses.add(time - 1, newPose);
    }

    public List<TrackedObject> getWaitingTrackedObjects() {
        return waitingTrackedObjects;
    }
    public void clearWaitingTrackedObjects() {
        waitingTrackedObjects.clear();
    }


    private List<CloudPoint> calculateLandMarkCloudPoints(TrackedObject tracked, Pose pose) {
        List<CloudPoint> landMarCloudPoints = new ArrayList<>();
        double poseX = pose.getX();
        double poseY = pose.getY();
        double yaw = pose.getYaw();
        double radians = yaw * Math.PI/180;
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        for (int i = 0; i < tracked.getCoordinates().size(); i++) {
            double lidarX = tracked.getCoordinates().get(i).getX();
            double lidarY = tracked.getCoordinates().get(i).getY();
            double landMarkX = (cos*lidarX) - (sin*lidarY) + poseX;
            double landMarkY = (sin*lidarX) + (cos*lidarY) + poseY;
            landMarCloudPoints.add(i,new CloudPoint(landMarkX, landMarkY));
        }
        return landMarCloudPoints;
    }

     /**
     * @PRE:
     * - `tracked` must be a non-null instance of `TrackedObject`.
     * - `pose` must be a non-null instance of `Pose`.
     * - `tracked.getCoordinates()` must not be null and must contain valid coordinates.
     *
     * @POST:
     * - If no landmark with the same ID as `tracked.getId()` exists:
     *   - A new `LandMark` is created and added to the `landmarks` list.
     *   - The number of landmarks in the `StatisticalFolder` is incremented.
     * - If a landmark with the same ID as `tracked.getId()` exists:
     *   - Its `coordinates` list is updated with averaged values from the existing and new `CloudPoint` data.
     *   - Any additional `CloudPoint` objects from the new data are appended to the existing list.
     * - The `landmarks` list is updated to reflect the new or modified landmark.
     */
    public void addOrUpdateLandMark(TrackedObject tracked, Pose pose) {
        List<CloudPoint> newCloudPoints = calculateLandMarkCloudPoints(tracked, pose);
        int landmarkIndex = landmarkIndex(tracked);
        // new landmark
        if(landmarkIndex == -1) {
            landmarks.add(new LandMark(tracked.getId(), tracked.getDescription(), newCloudPoints));
            StatisticalFolder.getInstance().incrementNumLandmarks();
            System.out.println("numLandmarks:" + StatisticalFolder.getInstance().getNumLandmarks());
        }
        // existing landmark
        else {
            List<CloudPoint> landmarkCoordinates = landmarks.get(landmarkIndex).getCoordinates();
            int minSize = Math.min(landmarkCoordinates.size(), newCloudPoints.size());
            for (int i = 0; i < minSize; i++) {
                landmarkCoordinates.get(i).setX((newCloudPoints.get(i).getX() + landmarkCoordinates.get(i).getX())/2);
                landmarkCoordinates.get(i).setY((newCloudPoints.get(i).getY() + landmarkCoordinates.get(i).getY())/2);
            }
            // make sure
            for(int i = landmarkCoordinates.size(); i < newCloudPoints.size(); i ++) {
                landmarkCoordinates.add(newCloudPoints.get(i));
            }
        }
    }

    private int landmarkIndex(TrackedObject object) {
        for (int i = 0; i < landmarks.size(); i++) {
            if(object.getId().equals(landmarks.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }


    public void generateOutput(Output output) {
        output.generateNormalOutputFile();
    }

    
    public String getCrashedSensorId() {
        return crashedSensorId;
    }

    public void setCrashedSensorId(String crashedSensorId) {
        this.crashedSensorId = crashedSensorId;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getActiveSensors() {
        return activeSensors;
    }

    public void setActiveSensors(int currActiveSensors) {
        this.activeSensors = currActiveSensors;
    }

    public void incrementActiveSensors() {
        activeSensors++;
    }
    public void oneLessActiveSensor() {
        if(activeSensors > 0)
            activeSensors--;
        else {
            throw new RuntimeErrorException(new Error("cannot decrease active sensors bellow 0!"));
        }
    }

    

}