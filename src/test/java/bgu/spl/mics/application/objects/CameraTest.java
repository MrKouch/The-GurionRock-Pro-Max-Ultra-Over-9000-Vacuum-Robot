package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.CrashedBroadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class CameraTest {
    Camera camera;

    @BeforeEach
    void setUp() {
        HashMap<Integer, StampedDetectedObjects> objects = new HashMap<>();

        List<DetectedObject> firstTickObjects = new ArrayList<>();
        firstTickObjects.add(new DetectedObject("1", "1st object"));
        firstTickObjects.add(new DetectedObject("2", "firstTickObjects object"));
        StampedDetectedObjects firstTickData = new StampedDetectedObjects(1,firstTickObjects);
        objects.put(1, firstTickData);
        
        List<DetectedObject> secondTickObjects = new ArrayList<>();
        secondTickObjects.add(new DetectedObject("3", "3rd object"));
        secondTickObjects.add(new DetectedObject("4", "4th object"));
        StampedDetectedObjects secondTickData = new StampedDetectedObjects(2,secondTickObjects);
        objects.put(2, secondTickData);
        
        List<DetectedObject> thirdObjects = new ArrayList<>();
        thirdObjects.add(new DetectedObject("5", "5th object"));
        secondTickObjects.add(new DetectedObject("ERROR", "ERROR object"));
        thirdObjects.add(new DetectedObject("6", "6th object"));
        StampedDetectedObjects thirdTickData = new StampedDetectedObjects(3,thirdObjects);
        objects.put(3, thirdTickData);
        
        List<DetectedObject> forthObjects = new ArrayList<>();
        forthObjects.add(new DetectedObject("7", "7th object"));
        forthObjects.add(new DetectedObject("8", "8th object"));
        StampedDetectedObjects forthTickData = new StampedDetectedObjects(4,forthObjects);
        objects.put(4, forthTickData);
        
        camera = new Camera(1,0, STATUS.UP, objects, true, 3);
    }

    /**@inv TODO
     * @pre TODO
     * @post TODO
     */
    @Test
    void operateTickTest() {
        assertEquals(1, camera.getId());
        assertEquals(0, camera.getFrequency());
        assertEquals(4, camera.getDetectedObjects().size());

        Message m = camera.operateTick(1);
        assertEquals(DetectObjectsEvent.class, m.getClass());
        assertEquals(1, camera.getLatestDetectionTime());
        m = camera.operateTick(2);
        assertEquals(DetectObjectsEvent.class, m.getClass());
        assertEquals(2, camera.getLatestDetectionTime());

        assertEquals(1, camera.getId());
        assertEquals(0, camera.getFrequency()););
        assertEquals(4, camera.getDetectedObjects().size());
    }

    /**@inv TODO
     * @pre TODO
     * @post TODO
     */
    @Test
    void operateTickErrorTest() {
        assertEquals(1, camera.getId());
        assertEquals(0, camera.getFrequency());
        assertEquals(4, camera.getDetectedObjects().size());

        Message m = camera.operateTick(3);
        assertEquals(CrashedBroadcast.class, m.getClass());
        assertEquals(STATUS.ERROR, camera.getStatus());

        assertEquals(1, camera.getId());
        assertEquals(0, camera.getFrequency());
        assertEquals(4, camera.getDetectedObjects().size());
    }
}