package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.application.services.PoseService;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Output {
    
    private String outputFilePath;
    
    public Output(String inputFilePath) {
        // Set the output file path to the same directory as the input configuration file
        System.out.println("write to: " + Paths.get(inputFilePath).getParent());
        this.outputFilePath = Paths.get(inputFilePath).getParent().resolve("output2setty.json").toString();
    }
    
    public void generateNormalOutputFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LinkedHashMap<String, Object> outputData = new LinkedHashMap<>();
        
        // Fetch statistical data
        StatisticalFolder statistics = StatisticalFolder.getInstance();
        
        outputData.put("systemRunTime", statistics.getSystemRuntime());
        outputData.put("numdetectedobjects", statistics.getNumDetectedObjects());
        outputData.put("numtrackedobjects", statistics.getNumTrackedObjects());
        outputData.put("numlandmarks", statistics.getNumLandmarks());
        outputData.put("landmarks", FusionSlam.getInstance().getLandmarks());
        
        // Write data to JSON file
        writeToFile(gson.toJson(outputData));
    }
    
    public void generateErrorOutputFile(HashMap<Camera, StampedDetectedObjects> lastDetectedObjects,
    HashMap<LiDarWorkerTracker, List<TrackedObject>> lastTrackedObjects,
    String faultySensor, String errorDescription) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LinkedHashMap<String, Object> outputData = new LinkedHashMap<>();
        
        outputData.put("error", errorDescription);
        outputData.put("faultySensor", faultySensor);
        // Add error-specific details
        
        // Add last detected objects for each camera
        HashMap<String, Object> cameraData = new HashMap<>();
        for (Camera camera : lastDetectedObjects.keySet()) {
            cameraData.put("Camera " + camera.getId(), lastDetectedObjects.get(camera));
        }
        outputData.put("lastCameraFrames", cameraData);
        
        // Add last tracked objects for each LiDAR worker
        HashMap<String, Object> lidarData = new HashMap<>();
        for (LiDarWorkerTracker workerTracker : lastTrackedObjects.keySet()) {
            lidarData.put("LiDarWorker_" + workerTracker.getId(), lastTrackedObjects.get(workerTracker));
        }
        outputData.put("lastLidarFrames", lidarData);

        outputData.put("poses: ", FusionSlam.getInstance().getposes());
        
        // Fetch statistical data
        StatisticalFolder statistics = StatisticalFolder.getInstance();
        LinkedHashMap<String, Object> statisticsData = new LinkedHashMap<>();
        statisticsData.put("systemRuntime", statistics.getSystemRuntime());
        statisticsData.put("numDetectedObjects", statistics.getNumDetectedObjects());
        statisticsData.put("numTrackedObjects", statistics.getNumTrackedObjects());
        statisticsData.put("numLandmarks", statistics.getNumLandmarks());
        statisticsData.put("landmarks", FusionSlam.getInstance().getLandmarks());

        // Add the statistics data to the main output map
        outputData.put("statistics", statisticsData);

        // Write data to JSON file
        writeToFile(gson.toJson(outputData));
    }

    private void writeToFile(String jsonData) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(jsonData);
            System.out.println("Output written to: " + outputFilePath);
        } catch (IOException e) {
            System.out.println("IOException e - Output");
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }
}
