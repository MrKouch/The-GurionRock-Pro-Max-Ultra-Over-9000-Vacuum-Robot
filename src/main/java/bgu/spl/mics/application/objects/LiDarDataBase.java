package bgu.spl.mics.application.objects;
import java.util.LinkedList;
import java.util.List;
/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private List<TrackedObject> trackedObjects;


    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase();
    }

    // Constructor
    private LiDarDataBase() {
        this.trackedObjects = new LinkedList<>();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance() {
        return LiDarDataBaseHolder.instance;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public void setTrackedObjects(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

}
