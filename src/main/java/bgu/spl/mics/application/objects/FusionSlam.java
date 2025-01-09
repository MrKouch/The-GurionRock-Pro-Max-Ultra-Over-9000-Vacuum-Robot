package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private List<LandMark> landmarks; // Should be implemented as an array
    private List<Pose> poses; // List of previous poses needed for calculations
    private List<TrackedObject> waitingTrackedObjects;
    private int activeSensors;
    private String crashedSensorId;
    private String errorDescription;

    // Constructor
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.waitingTrackedObjects = new LinkedList<>();
         // todo: initialize activeSensors
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
    // Getters and Setters
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public LandMark getLandmark(String id) {
        for (LandMark landmark : landmarks) {
            if (landmark.getId() == id) {
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

    public Pose getPose(int time) {
        return poses.get(time);
    }

    public void addPose(int time, Pose newPose) {
        this.poses.add(time, newPose);
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

    public void addOrUpdateLandMark(TrackedObject tracked, Pose pose) {
        List<CloudPoint> newCloudPoints = calculateLandMarkCloudPoints(tracked, pose);
        int landmarkIndex = landmarkIndex(tracked);
        // new landmark
        if(landmarkIndex == -1) {
            landmarks.add(new LandMark(tracked.getId(), tracked.getDescription(), newCloudPoints));
            StatisticalFolder.getInstance().incrementNumLandmarks();
        }
        // existing landmark
        else {
            List<CloudPoint> landmarkCoordinates = landmarks.get(landmarkIndex).getCoordinates();
            for (int i = 0; i < landmarkCoordinates.size(); i++) {
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
            if(object.getId() == landmarks.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }


    public Output generateOutput(Output output) {
        // Go the the statistical folder and add the data from there to a json file containing:
        // numdetectedobjects
        // numtrackedobjects
        // numlandmarks
        // list of the kandmarks with all the data about them in the shape of (example):
        // "Wall_4":{"id":"Wall_4","description":"Wall","coordinates":[{"x":1.1887387639977982,"y":5.046603301251042},{"x":1.2533775541582042,"y":5.113604111414717}]},
        return null;
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

}