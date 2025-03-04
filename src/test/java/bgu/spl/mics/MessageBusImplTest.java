package bgu.spl.mics;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {
    private MessageBusImpl theBus;
    private CameraService ms1;

    @BeforeEach
    void setup(){
        theBus = MessageBusImpl.getMessageBus();
        HashMap<Integer, StampedDetectedObjects> objects = new HashMap<>();
        ms1 = new CameraService(new Camera(1, 1, objects));
        theBus.register(ms1);
    }

    @Test
    void registerTest() {
        HashMap<Integer, StampedDetectedObjects> objects2 = new HashMap<>();
        CameraService ms2 = new CameraService(new Camera(2, 2, objects2)); // Example MicroServices
        assertFalse(theBus.getRegisteredMS().containsKey(ms2));
        theBus.register(ms2);
        assertTrue(theBus.getRegisteredMS().containsKey(ms2));
    }

    @Test
    void unregisterTest() {
        assertTrue(theBus.getRegisteredMS().containsKey(ms1));
        theBus.unregister(ms1);
        assertFalse(theBus.getRegisteredMS().containsKey(ms1));
    }
    
    @Test
    void subscribeBroadcastTest() {
        assertTrue(theBus.getBroadcastsSubscribers().isEmpty());
        MicroService camService = new CameraService(new Camera(1, 5, new HashMap<>()));
        theBus.subscribeBroadcast(CrashedBroadcast.class, camService);
        LinkedBlockingQueue<MicroService> subscribers = theBus.getBroadcastsSubscribers().get(CrashedBroadcast.class);
        assertTrue(subscribers.contains(camService));
    }
}