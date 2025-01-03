package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private String sensorID;
    private String crashedBecause;

    public CrashedBroadcast(String sensorID, String crashedBecause) {
        this.sensorID = sensorID;
        this.crashedBecause = crashedBecause;
    }

    public String getSensorID() {
        return sensorID;
    }

    public String getCrashedBecause() {
        return crashedBecause;
    }

}
