package bgu.spl.mics.application.services;

import javax.sound.midi.Track;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    
    private FusionSlam fusionSlam;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = FusionSlam.getInstance();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tickBroadCast -> {
            
        });

        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            for(TrackedObject object : trackedObjectsEvent.getTrackedObjects()) {
                Pose currentPose = fusionSlam.getposes().get(object.getTime());
                if(currentPose == null) {
                    fusionSlam.getWaitingTrackedObjects().add(object.getTime(), object);
                }
                else {
                    fusionSlam.addOrUpdateLandMark(object, currentPose);
                }
            }
        });

        subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose currPose = poseEvent.getCurrentPose();
            fusionSlam.getposes().add(currPose.getTime(), currPose);
            for(TrackedObject object : fusionSlam.getWaitingTrackedObjects()) {
                fusionSlam.addOrUpdateLandMark(object, currPose);
                //fusionSlam.addOrUpdateLandMark(object, fusionSlam.getposes().get(object.getTime()));
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {

        });

        subscribeBroadcast(CrashedBroadcast.class, (crashedBroadcast) -> {
            this.fusionSlam.setCrashedSensorId(crashedBroadcast.getSensorID());
            this.fusionSlam.setErrorDescription((crashedBroadcast.getCrashedBecause()));
        });
    }
}
