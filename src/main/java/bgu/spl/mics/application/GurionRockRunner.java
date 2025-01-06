package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Input;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.TimeService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {
    /**
 * The main method of the simulation.
 * This method sets up the necessary components, parses configuration files,
 * initializes services, and starts the simulation.
 *
 * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
 */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Configuration file path not provided.");
            return;
        }

        try {
            // Parse the input configuration
            Input input = new Input(args[0]);

            // Create and start TimeService
            int tickTime = input.getTickTime();
            int duration = input.getDuration();
            TimeService timeService = new TimeService(tickTime, duration);
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();

            // Create and start CameraServices
            List<Thread> serviceThreads = new ArrayList<>();
            for (Camera camera : input.getCameras()) {
                CameraService cameraService = new CameraService(camera);
                Thread cameraThread = new Thread(cameraService);
                cameraThread.start();
                serviceThreads.add(cameraThread);
            }

            // Create and start LiDarServices
            for (LiDarWorkerTracker lidarWorker : input.getLidarWorkers()) {
                LiDarService lidarService = new LiDarService(lidarWorker);
                Thread lidarThread = new Thread(lidarService);
                lidarThread.start();
                serviceThreads.add(lidarThread);
            }

            // Wait for the simulation to finish
            timeServiceThread.join(); // Wait for TimeService to complete
            for (Thread serviceThread : serviceThreads) {
                serviceThread.join(); // Wait for all services to complete
            }

            // Output final results (e.g., to a JSON file)
            System.out.println("Simulation completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}





