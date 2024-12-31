package bgu.spl.mics.application.messages;
import java.util.List;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
public class TrackedObjectsEvent implements Event<String> {
// Sent by: a LiDar worker
// • Handled by: Fusion-SLAM
// • Details:
//      o Includes a list of TrackedObjects.
//      o Upon receiving this event, Fusion:
//          ▪ Transforms the cloud points to the charging station's coordinate system using the
//              current pose.
//          ▪ Checks if the object is new or previously detected:
//                  • If new, add it to the map.
//                  • If previously detected, updates measurements by averaging with previous
//                      data.

    // Fields
    private final String senderID;
    private List<TrackedObject> trackedObjects;

    // Constructor
    public TrackedObjectsEvent(String senderID, List<TrackedObject> trackedObjects) {
        this.senderID = senderID;
        this.trackedObjects = trackedObjects;
    }

    public String getSenderID() {
        return senderID;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

}
