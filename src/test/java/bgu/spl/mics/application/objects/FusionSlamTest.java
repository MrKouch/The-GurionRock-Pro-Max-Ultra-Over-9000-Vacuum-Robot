package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FusionSlamTest {
    private FusionSlam fusionSlam;
    private List<CloudPoint> cloudPoints1;
    private List<CloudPoint> cloudPoints2;
    private List<CloudPoint> cloudPoints3;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear();

        cloudPoints1 = new ArrayList<>(Arrays.asList(
                new CloudPoint(0.1176, 3.6969),
                new CloudPoint(0.11362, 3.6039)
        ));

        cloudPoints2 = new ArrayList<>(Arrays.asList(
                new CloudPoint(0.5, 3.9),
                new CloudPoint(0.2, 3.7)
        ));

        cloudPoints3 = new ArrayList<>(Arrays.asList(
                new CloudPoint(0.5, -2.1),
                new CloudPoint(0.8, -2.3)
        ));

        fusionSlam.addPose(1, new Pose(-3.2076f, 0.0755f, -87.48f, 1));
        fusionSlam.addPose(2, new Pose(-2.366f, 0.9327f, -28.08f, 2));
        fusionSlam.addPose(3, new Pose(0.0f, 3.6f, 57.3f, 3));
    }

    @Test
    void createLandMarks() {
        assertTrue(fusionSlam.getLandmarks().isEmpty());

        TrackedObject trackedObject1 = new TrackedObject("1", 1, "Setty", cloudPoints1);
        TrackedObject trackedObject2 = new TrackedObject("2", 2, "Kouch", cloudPoints2);
        TrackedObject trackedObject3 = new TrackedObject("3", 3, "Amaritos", cloudPoints3);

        fusionSlam.addOrUpdateLandMark(trackedObject1, fusionSlam.getPose(1));
        fusionSlam.addOrUpdateLandMark(trackedObject2, fusionSlam.getPose(2));
        fusionSlam.addOrUpdateLandMark(trackedObject3, fusionSlam.getPose(3));
        
        LandMark landmark1 = fusionSlam.getLandmark("1");
        assertNotNull(landmark1);
        assertEquals(2, landmark1.getCoordinates().size());
        
        LandMark landmark2 = fusionSlam.getLandmark("2");
        assertNotNull(landmark2);
        assertEquals(2, landmark2.getCoordinates().size());
    }
    
    @Test
    void createLandMarksWithExtraPoints() {
        assertTrue(fusionSlam.getLandmarks().isEmpty());
        
        cloudPoints2.add(new CloudPoint(0.1, 3.6));
        cloudPoints2.add(new CloudPoint(7.0, 8.0));
        
        TrackedObject trackedObject1 = new TrackedObject("1", 1, "Setty", cloudPoints1);
        TrackedObject trackedObject2 = new TrackedObject("2", 2, "Kouch", cloudPoints2);
        TrackedObject trackedObject3 = new TrackedObject("2", 3, "Kouch", cloudPoints2);

        fusionSlam.addOrUpdateLandMark(trackedObject1, fusionSlam.getPose(1));
        fusionSlam.addOrUpdateLandMark(trackedObject2, fusionSlam.getPose(2));
        fusionSlam.addOrUpdateLandMark(trackedObject3, fusionSlam.getPose(3));
        
        LandMark landmark = fusionSlam.getLandmark("1");
        assertNotNull(landmark);
        assertEquals(2, landmark.getCoordinates().size());

        LandMark landmark2 = fusionSlam.getLandmark("2");
        assertNotNull(landmark2);
        assertEquals(4, landmark2.getCoordinates().size());
    }
}