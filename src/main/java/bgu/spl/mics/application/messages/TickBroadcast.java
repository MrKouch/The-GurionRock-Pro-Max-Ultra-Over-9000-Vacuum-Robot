package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    
    // Fields
    private int currentTime;

    // Constructor
    public TickBroadcast(int currTime) {
        this.currentTime = currTime;
    }

    public int getCurrentTime() {
        return currentTime;
    }
}
