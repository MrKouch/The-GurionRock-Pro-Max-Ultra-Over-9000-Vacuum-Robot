package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.Event;

public class DetectObjectsEvent implements Event<Boolean> {
    
    // Fields
    StampedDetectedObjects stampedDetectedObjects;
    
    
    public DetectObjectsEvent(StampedDetectedObjects DetectedObjects) {
        this.stampedDetectedObjects = DetectedObjects;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }
}