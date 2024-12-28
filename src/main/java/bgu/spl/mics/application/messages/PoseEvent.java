package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<String> {
    
    // Fields
    private Pose currentPose;

    // Constructor
    public PoseEvent(Pose currPose) {
        this.currentPose = currPose;
    }

    public Pose getCurrentPose() {
        return currentPose;
    }
}
