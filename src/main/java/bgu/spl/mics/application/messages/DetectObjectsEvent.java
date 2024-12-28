package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class DetectObjectsEvent implements Event<Boolean> {
    List<StampedDetectedObjects> newDetectedObjects;
//  Sent by: Camera
// • Handled by: a LiDar Worker
// • Details:
//      o Includes DetectedObjects.
//      o The Camera send DetectedObjectsEvent of the Objects with time T for all the
//          subscribed Lidar workers to this event at time T, and one of them deals with a single
//          event.

    // Fields
    
    public DetectObjectsEvent(List<StampedDetectedObjects> newDetectedObjects) {
        this.newDetectedObjects = newDetectedObjects;
    }
}