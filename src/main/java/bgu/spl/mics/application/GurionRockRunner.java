package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Input;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Output;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

    public static CountDownLatch latch;
    public static void main(String[] args) {
        //TODO: make sure that the TimeService thread starts last.
        //TODO: create the fusionSlam thread
        //TODO: run examples
        
        if (args.length == 0) {
            System.err.println("Error: Configuration file path not provided.");
            return;
        }

        try {
            // Parse the input configuration
            Input input = new Input(args[0]);

            // Create a CountDownLatch with the total number of service threads
            int totalServices = input.getCameras().size() + input.getLidarWorkers().size() + 2;
            latch = new CountDownLatch(totalServices);

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
            
            PoseService poseService = new PoseService(input.getGpsimu());
            Thread posesThread = new Thread(poseService);
            posesThread.start();
            serviceThreads.add(posesThread);
            
            FusionSlamService fusionSlamService = new FusionSlamService();
            Thread fusionSlamThread = new Thread(fusionSlamService);
            fusionSlamThread.start();
            serviceThreads.add(fusionSlamThread);

            // Wait for all service threads to complete their initialization
            latch.await();
            // Create and start TimeService
            int tickTime = input.getTickTime();
            int duration = input.getDuration();
            TimeService timeService = new TimeService(tickTime, duration);
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();
            
            // Wait for the simulation to finish
            timeServiceThread.join(); // Wait for TimeService to complete
            int i = 0;
            for (Thread serviceThread : serviceThreads) {
                System.out.println("iteration " + i);
                i++;
                serviceThread.join(); // Wait for all services to complete
            }
            
            // Output final results (e.g., to a JSON file)
            System.out.println("Simulation completed successfully!");
            // Output output = new Output(args[0]);
            // FusionSlam fusionSlam = FusionSlam.getInstance();
            // if(fusionSlam.getCrashedSensorId() == "") {
            //     fusionSlam.generateOutput(output);
            // }
            // else {
            //     HashMap<Camera, StampedDetectedObjects> lastDetectedObjects = new HashMap<>();
            //     HashMap<LiDarWorkerTracker, List<TrackedObject>> lastTrackedObjects = new HashMap<>();
            //     // iterate over all the sensors and get the
            //     for (Camera camera : input.getCameras()) {
            //         lastDetectedObjects.put(camera, camera.getLastDetectedObjects());
                    
            //     }
            //     for (LiDarWorkerTracker workerTracker : input.getLidarWorkers()) {
            //         lastTrackedObjects.put(workerTracker, workerTracker.getLastTrackedObjects());
            //     }
            //     output.generateErrorOutputFile(lastDetectedObjects, lastTrackedObjects, fusionSlam.getCrashedSensorId(), fusionSlam.getErrorDescription()); 
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}