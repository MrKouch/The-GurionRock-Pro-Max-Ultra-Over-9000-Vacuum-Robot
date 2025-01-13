package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
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
    
    public FusionSlamService() {
        super("FusionSlamService");
        this.fusionSlam = FusionSlam.getInstance();
    }

    private void handleSensorTerminated(String whyTerminated) {
        this.fusionSlam.oneLessActiveSensor();
        int currActiveSensors = this.fusionSlam.getActiveSensors();
        if (currActiveSensors == 0) {
            sendBroadcast(new TerminatedBroadcast(FusionSlamService.class, whyTerminated));
            this.terminate();
        }
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose currPose = poseEvent.getCurrentPose();
            fusionSlam.addPose(currPose.getTime(), currPose);
            for(TrackedObject object : fusionSlam.getWaitingTrackedObjects()) {
                fusionSlam.addOrUpdateLandMark(object, currPose);
            }
            fusionSlam.clearWaitingTrackedObjects();
        });
        
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            for(TrackedObject object : trackedObjectsEvent.getTrackedObjects()) {
                Pose currentPose = fusionSlam.getPose(object.getTime());
                if(currentPose == null) {
                    fusionSlam.getWaitingTrackedObjects().add(object);
                }
                else {
                    fusionSlam.addOrUpdateLandMark(object, currentPose);
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getServiceWhoTerminated() == TimeService.class) {
                this.terminate();
            }
            else {
                handleSensorTerminated("finished");
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (crashedBroadcast) -> {
            this.fusionSlam.setCrashedSensorId(crashedBroadcast.getSensorID());
            this.fusionSlam.setErrorDescription((crashedBroadcast.getCrashedBecause()));
            handleSensorTerminated(crashedBroadcast.getCrashedBecause());
        });
    }
}
