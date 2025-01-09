package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class Output {

    private String outputFilePath;

    public Output(String inputFilePath) {
        // Set the output file path to the same directory as the input configuration file
        this.outputFilePath = Paths.get(inputFilePath).getParent().resolve("output.json").toString();
    }

    public void generateNormalOutputFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, Object> outputData = new HashMap<>();

        // Fetch statistical data
        StatisticalFolder statistics = StatisticalFolder.getInstance();
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
        HashMap<String, Object> outputData = new HashMap<>();

        // Add error-specific details
        outputData.put("faultySensor", faultySensor);
        outputData.put("errorDescription", errorDescription);

        // Add last detected objects for each camera
        HashMap<String, Object> cameraData = new HashMap<>();
        for (Camera camera : lastDetectedObjects.keySet()) {
            cameraData.put("Camera_" + camera.getId(), lastDetectedObjects.get(camera));
        }
        outputData.put("lastDetectedObjects", cameraData);

        // Add last tracked objects for each LiDAR worker
        HashMap<String, Object> lidarData = new HashMap<>();
        for (LiDarWorkerTracker workerTracker : lastTrackedObjects.keySet()) {
            lidarData.put("LiDarWorker_" + workerTracker.getId(), lastTrackedObjects.get(workerTracker));
        }
        outputData.put("lastTrackedObjects", lidarData);

        // Fetch statistical data
        StatisticalFolder statistics = StatisticalFolder.getInstance();
        outputData.put("numdetectedobjects", statistics.getNumDetectedObjects());
        outputData.put("numtrackedobjects", statistics.getNumTrackedObjects());
        outputData.put("numlandmarks", statistics.getNumLandmarks());
        outputData.put("landmarks", FusionSlam.getInstance().getLandmarks());

        // Write data to JSON file
        writeToFile(gson.toJson(outputData));
    }

    private void writeToFile(String jsonData) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(jsonData);
            System.out.println("Output written to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }
}
