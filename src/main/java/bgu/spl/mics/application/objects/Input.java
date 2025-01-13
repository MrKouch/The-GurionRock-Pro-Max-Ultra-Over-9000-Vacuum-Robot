package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Input {
    private List<Camera> cameras;
    private List<LiDarWorkerTracker> lidarWorkers;
    private GPSIMU gpsimu;
    private int tickTime;
    private int duration;

    public Input(String configFilePath) throws IOException {
        Gson gson = new Gson();

        // Parse the configuration file
        Map<String, Object> config;
        try (FileReader reader = new FileReader(configFilePath)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            config = gson.fromJson(reader, type);
        }

        if (config == null) {
            throw new IOException("Failed to parse the configuration file.");
        }

        String directory = new File(configFilePath).getParent();
        Map<String, Object> camerasConfig = (Map<String, Object>) config.get("Cameras");
        String cameraDataPath = directory + File.separator + camerasConfig.get("camera_datas_path");
        List<Map<String, Object>> cameraConfigurations = (List<Map<String, Object>>) camerasConfig.get("CamerasConfigurations");

        Map<String, Object> lidarConfig = (Map<String, Object>) config.get("LiDarWorkers");
        String lidarDataPath = directory + File.separator + lidarConfig.get("lidars_data_path");
        List<Map<String, Object>> lidarConfigurations = (List<Map<String, Object>>) lidarConfig.get("LidarConfigurations");

        String poseDataPath = directory + File.separator + config.get("poseJsonFile").toString();

        // Parse camera data
        this.cameras = parseCameraData(cameraDataPath, cameraConfigurations);

        // Parse LiDAR data
        this.lidarWorkers = parseLidarWorkers(lidarDataPath, lidarConfigurations);

        // Parse pose data
        this.gpsimu = parsePoseData(poseDataPath);

        // Parse TickTime and Duration
        this.tickTime = ((Double) config.get("TickTime")).intValue();
        this.duration = ((Double) config.get("Duration")).intValue();
    }

    private List<Camera> parseCameraData(String filePath, List<Map<String, Object>> cameraConfigurations) throws IOException {
        Gson gson = new Gson();
        Map<String, List<Map<String, Object>>> rawCameraData;
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType();
            rawCameraData = gson.fromJson(reader, type);
        }
    
        List<Camera> cameras = new LinkedList<>();
        for (Map<String, Object> config : cameraConfigurations) {
            int id = ((Double) config.get("id")).intValue();
            int frequency = ((Double) config.get("frequency")).intValue();
            String cameraKey = (String) config.get("camera_key");

            HashMap<Integer, StampedDetectedObjects> detectedObjects = new HashMap<>();
    
            if (rawCameraData.containsKey(cameraKey)) {
                for (Map<String, Object> detected : rawCameraData.get(cameraKey)) {
                    int time = ((Double) detected.get("time")).intValue();
                    List<DetectedObject> objects = gson.fromJson(
                        gson.toJson(detected.get("detectedObjects")),
                        new TypeToken<List<DetectedObject>>() {}.getType()
                    );
                    detectedObjects.put(time, new StampedDetectedObjects(time, objects));
                }
            }
    
            cameras.add(new Camera(id, frequency, detectedObjects));
        }
        return cameras;
    }

    private List<LiDarWorkerTracker> parseLidarWorkers(String filePath, List<Map<String, Object>> lidarConfigurations) throws IOException {
        Gson gson = new Gson();
        List<Map<String, Object>> rawLidarData;
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            rawLidarData = gson.fromJson(reader, type);
        }
    
        // Store tracked objects in LiDarDataBase
        LiDarDataBase lidarDatabase = LiDarDataBase.getInstance();
        List<TrackedObject> trackedObjects = new LinkedList<>();
    
        for (Map<String, Object> tracked : rawLidarData) {
            String id = (String) tracked.get("id");
            int time = ((Double) tracked.get("time")).intValue();
            List<List<Double>> cloudPoints = gson.fromJson(
                gson.toJson(tracked.get("cloudPoints")),
                new TypeToken<List<List<Double>>>() {}.getType()
            );
    
            List<CloudPoint> coordinates = new LinkedList<>();
            for (List<Double> point : cloudPoints) {
                double x = point.get(0);
                double y = point.get(1);
                coordinates.add(new CloudPoint(x, y));
            }
    
            trackedObjects.add(new TrackedObject(id, time, getDescription(id), coordinates));
        }
    
        lidarDatabase.setTrackedObjects(trackedObjects);
    
        // Initialize LiDarWorkerTracker objects
        List<LiDarWorkerTracker> lidarWorkers = new LinkedList<>();
        for (Map<String, Object> config : lidarConfigurations) {
            String id = String.valueOf(((Double) config.get("id")).intValue());
            int frequency = ((Double) config.get("frequency")).intValue();
    
            // LiDarWorkerTrackers start with empty stampedDetectedObjects
            lidarWorkers.add(new LiDarWorkerTracker(id, frequency, new LinkedList<>()));
        }
        return lidarWorkers;
    }
    

    private GPSIMU parsePoseData(String filePath) throws IOException {
        Gson gson = new Gson();
        List<Map<String, Object>> rawPoseData;
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            rawPoseData = gson.fromJson(reader, type);
        }

        List<Pose> poses = new LinkedList<>();
        for (Map<String, Object> poseMap : rawPoseData) {
            float x = ((Double) poseMap.get("x")).floatValue();
            float y = ((Double) poseMap.get("y")).floatValue();
            float yaw = ((Double) poseMap.get("yaw")).floatValue();
            int time = ((Double) poseMap.get("time")).intValue();
            poses.add(new Pose(x, y, yaw, time));
        }
        return new GPSIMU(0, poses);
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    private String getDescription(String id) {
        for (Camera camera : cameras) {
            for (Integer timeKey : camera.getDetectedObjects().keySet()) {
                StampedDetectedObjects stamped = camera.getDetectedObjects().get(timeKey);
                for (DetectedObject detectedObject : stamped.getDetectedObjects()) {
                    if (detectedObject.getId().equals(id))
                        return detectedObject.getDescription();
                }
            }
        }
        return "default";
    }

    public List<LiDarWorkerTracker> getLidarWorkers() {
        return lidarWorkers;
    }

    public GPSIMU getGpsimu() {
        return gpsimu;
    }

    public int getTickTime() {
        return tickTime;
    }
    
    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration Details:\n");
    
        sb.append("Cameras:\n");
        for (Camera camera : getCameras()) {
            sb.append(camera.toString()).append("\n");
        }
    
        sb.append("LiDar Workers:\n");
        for (LiDarWorkerTracker lidarWorker : getLidarWorkers()) {
            sb.append(lidarWorker.toString()).append("\n");
        }
    
        sb.append("Tick Time: ").append(getTickTime()).append("\n");
        sb.append("Duration: ").append(getDuration()).append("\n");
    
        return sb.toString();
    }
}
