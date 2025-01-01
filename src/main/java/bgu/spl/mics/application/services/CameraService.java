package bgu.spl.mics.application.services;

import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
// import bgu.spl.mics.application.messages.*;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    Camera camera;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera"+Integer.toString(camera.getId()));
        this.camera = camera;
    }

    private StampedDetectedObjects getReadyDetectedObjects(int currentTime) {
        List<StampedDetectedObjects> detectedObjects = camera.getDetectedObjectsList();
        for (StampedDetectedObjects objectsDetectedInTime : detectedObjects) {
            if (objectsDetectedInTime.getTime() + camera.getFrequency() == currentTime)
                return objectsDetectedInTime;
        }
        return null;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            System.out.println("Camera " + getName() + " got a new TickBroadcast");
            int currentTime = tickBroadcast.getCurrentTime();

            StampedDetectedObjects readyDetectedObjects = getReadyDetectedObjects(currentTime);
            if (readyDetectedObjects != null) {
                Future<Boolean> futureObject = (Future<Boolean>)sendEvent(new DetectObjectsEvent(readyDetectedObjects));
                if (futureObject != null) {
                    // the futureObject.get() is a blocking method - make sure it's okay
                    Boolean resolved = futureObject.get();
                    if (resolved) {
                        System.out.println("The object's coordinations has been successfully processed by a LiDARWorker and the Fusion.");
                    }
                    else {
                        System.out.println("FAILED: The object's coordinations has not been successfully processed by a LiDARWorker and the Fusion.");
                    }
                }
                else {
                    System.out.println("No Micro-Service has registered to handle DetectObjectsEvent events! The event cannot be processed");
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println("Camera " + getName() + " is being terminated");
        });
    }
}
