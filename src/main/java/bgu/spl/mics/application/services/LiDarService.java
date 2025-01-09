package bgu.spl.mics.application.services;

import java.util.LinkedList;
import java.util.List;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    // Fields
    private LiDarWorkerTracker liDarWorkerTracker;
    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDAR_ID: " + LiDarWorkerTracker.getId());
        this.liDarWorkerTracker = LiDarWorkerTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println("LiDAR Service " + getName() + " has started");

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int currentTime = tickBroadcast.getCurrentTime();
            Message msg = liDarWorkerTracker.operateTick(currentTime, getName());
            if (msg instanceof TerminatedBroadcast) {
                sendBroadcast((TerminatedBroadcast)msg);
                liDarWorkerTracker.setStatus(STATUS.DOWN);
                this.terminate();
            }
            else if (msg instanceof CrashedBroadcast) {
                sendBroadcast((CrashedBroadcast)msg);
                liDarWorkerTracker.setStatus(STATUS.ERROR);
                this.terminate();
            }
            else if (msg instanceof TrackedObjectsEvent) {
                // Transfer the latest tracked objects data to the fusionSLAM using the message bus
                sendEvent((TrackedObjectsEvent)msg);
                // Empty the last tracked objects list
                liDarWorkerTracker.getWaitingObjects().clear();
            }
        });

        subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {
            liDarWorkerTracker.getStampedDetectedObjects().add((detectObjectsEvent).getStampedDetectedObjects());
        });

        // NOT SURE
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getServiceWhoTerminated() == TimeService.class) {
                sendBroadcast(new TerminatedBroadcast(LiDarService.class, "lidar - The time has reached the Duration limit."));
                liDarWorkerTracker.setStatus(STATUS.DOWN);
                this.terminate();
            }
        });

        // NOT SURE
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            if (liDarWorkerTracker.getLastDetectionTime() == crashedBroadcast.getCrashedTime())
                liDarWorkerTracker.setLastTrackedObjectsToPrev();
            sendBroadcast(new TerminatedBroadcast(LiDarService.class, "lidar - other sensor has been creshed."));
            liDarWorkerTracker.setStatus(STATUS.DOWN);
            this.terminate();
        });

    }
}
