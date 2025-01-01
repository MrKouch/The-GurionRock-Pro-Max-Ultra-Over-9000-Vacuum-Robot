package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class TerminatedBroadcast implements Broadcast {
    Class<? extends MicroService> serviceWhoTerminated;
    String terminatedBecause;
    public TerminatedBroadcast(Class<? extends MicroService> serviceWhoTerminated, String terminatedBecause) {
        this.serviceWhoTerminated = serviceWhoTerminated;
        this.terminatedBecause = terminatedBecause;
    }
}
