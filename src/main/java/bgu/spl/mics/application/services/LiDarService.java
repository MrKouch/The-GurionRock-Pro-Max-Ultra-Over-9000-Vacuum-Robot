package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;

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
        subscribeBroadcast(TerminatedBroadcast.class, terminationEvent -> {
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, stopNow -> {
            throw new RuntimeException("LiDAR with ID: " + liDarWorkerTracker.getId() + "stops now because " + stopNow.getCrashedBecause());
        });
        //TODO: HANDLE THE TIME-STAMPING OF THE OBJECTS
        subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {
            sendEvent(new TrackedObjectsEvent(liDarWorkerTracker.getId(), liDarWorkerTracker.getLastTrackedObjects()));
        });

    }
}
