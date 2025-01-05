package bgu.spl.mics.application.services;

import java.util.LinkedList;
import java.util.List;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
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
        System.out.println("LiDAR Service " + getName() + " has srarted");

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            liDarWorkerTracker.findTrackedObjects(tickBroadcast.getCurrentTime());

            //maybe synchronize the following lines somehow
            sendEvent(new TrackedObjectsEvent(getName(), liDarWorkerTracker.getLastTrackedObjects()));
            StatisticalFolder.getInstance().incrementNumTrackedObjects(liDarWorkerTracker.getLastTrackedObjects().size());
            ///////////
            
            liDarWorkerTracker.getLastTrackedObjects().clear();
        });

        subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {
            
            for(DetectedObject detectedObject : detectObjectsEvent.getStampedDetectedObjects().getDetectedObjects()) {
                TrackedObject trackedObject = new TrackedObject(detectedObject, detectObjectsEvent.getStampedDetectedObjects().getTime());
                liDarWorkerTracker.getLastTrackedObjects().add(trackedObject);
            }
        });

        // NOT SURE
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getServiceWhoTerminated() == TimeService.class) {
                this.terminate();
            }
        });

        // NOT SURE
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            this.terminate();
        });

    }
}
