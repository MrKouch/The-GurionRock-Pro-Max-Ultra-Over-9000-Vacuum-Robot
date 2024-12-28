package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class DetectObjectsEvent implements Event<Boolean> {
    List<StampedDetectedObjects> newDetectedObjects;
    
    public DetectObjectsEvent(List<StampedDetectedObjects> newDetectedObjects) {
        this.newDetectedObjects = newDetectedObjects;
    }
}