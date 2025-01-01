package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private List<StampedCloudPoints> cloudPoints;

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase();
    }

    // Constructor
    private LiDarDataBase() {
        this.cloudPoints = new ArrayList<StampedCloudPoints>();
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

    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}
