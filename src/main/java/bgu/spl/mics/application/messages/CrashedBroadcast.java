package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private String sensorID;
    private String crashedBecause;
    private int crashedTime;

    public CrashedBroadcast(String sensorID, String crashedBecause, int crashedTime) {
        this.sensorID = sensorID;
        this.crashedBecause = crashedBecause;
        this.crashedTime = crashedTime;
    }

    public String getSensorID() {
        return sensorID;
    }

    public String getCrashedBecause() {
        return crashedBecause;
    }

    public int getCrashedTime() {
        return crashedTime;
    }

}
