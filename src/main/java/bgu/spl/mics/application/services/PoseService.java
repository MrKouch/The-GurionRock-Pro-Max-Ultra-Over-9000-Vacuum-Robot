package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseSerice");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int currentTime = tickBroadcast.getCurrentTime();
            if (currentTime > gpsimu.getLatestDetectionTime()) {
                sendBroadcast(new TerminatedBroadcast(PoseService.class, "pose - finished"));
                gpsimu.setStatus(STATUS.DOWN);
                this.terminate();
            }
            Pose currentPose = gpsimu.getPoseByTick(currentTime);
            sendEvent(new PoseEvent(currentPose));
        });

        // NOT SURE
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getServiceWhoTerminated() == TimeService.class) {
                sendBroadcast(new TerminatedBroadcast(PoseService.class, "pose - The time has reached the Duration limit."));
                gpsimu.setStatus(STATUS.DOWN);
                this.terminate();
            }
        });

        // NOT SURE
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            sendBroadcast(new TerminatedBroadcast(PoseService.class, "pose - other sensor has been creshed."));
            gpsimu.setStatus(STATUS.DOWN);
            this.terminate();
        });
    }
}
