package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.FrequencyBroadcast;
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

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println("Camera Service " + getName() + " has started");
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int currentTime = tickBroadcast.getCurrentTime();
            Message msg = camera.operateTick(currentTime);
            if (msg instanceof FrequencyBroadcast)
                sendBroadcast((FrequencyBroadcast)msg);
            if (msg instanceof TerminatedBroadcast) {
                sendBroadcast((TerminatedBroadcast)msg);
                camera.setStatus(STATUS.DOWN);
                this.terminate();
            }
            else if (msg instanceof CrashedBroadcast) {
                sendBroadcast((CrashedBroadcast)msg);
                camera.setStatus(STATUS.ERROR);
                this.terminate();

            }
            else if (msg instanceof DetectObjectsEvent) {
                Future<Boolean> futureObject = sendEvent((DetectObjectsEvent)msg);
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            // make sure - what happens in terminate()?
            if (terminatedBroadcast.getServiceWhoTerminated() == TimeService.class) {
                sendBroadcast(new TerminatedBroadcast(CameraService.class, "camera - The time has reached the Duration limit."));
                camera.setStatus(STATUS.DOWN);
                this.terminate();
            }
        });

        // NOT SURE
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            if (camera.getLastDetectedObjects().getTime() == crashedBroadcast.getCrashedTime())
                camera.setLastDetectedObjectsToPrev();
            sendBroadcast(new TerminatedBroadcast(CameraService.class, "camera - other sensor has been creshed."));
            camera.setStatus(STATUS.DOWN);
            this.terminate();
        });
    }
}
