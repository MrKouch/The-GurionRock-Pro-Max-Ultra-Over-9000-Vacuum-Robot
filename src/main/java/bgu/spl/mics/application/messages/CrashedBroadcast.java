package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private String sensorName;
    private String sensorID;
    private String crashedBecause;

    public CrashedBroadcast(String sensorName, String sensorID, String crashedBecause) {
        this.sensorID = sensorID;
        this.crashedBecause = crashedBecause;
        this.sensorName = sensorName;
    }

    public String getSensorID() {
        return sensorID;
    }

    public String getCrashedBecause() {
        return getSensorName() + "ID: " + getSensorID() + crashedBecause;
    }

    public String getSensorName() {
        return sensorName;
    }
}
