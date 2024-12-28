package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast {
    int sensorId;
    String terminatedBecause;
    public TerminatedBroadcast(int sensorId, String terminatedBecause) {
        this.sensorId = sensorId;
        this.terminatedBecause = terminatedBecause;
    }
}
