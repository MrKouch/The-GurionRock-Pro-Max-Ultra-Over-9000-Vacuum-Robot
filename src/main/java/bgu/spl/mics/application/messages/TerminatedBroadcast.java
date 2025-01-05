package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class TerminatedBroadcast implements Broadcast {
    private Class<? extends MicroService> serviceWhoTerminated;
    private String terminatedBecause;

    public TerminatedBroadcast(Class<? extends MicroService> serviceWhoTerminated, String terminatedBecause) {
        this.serviceWhoTerminated = serviceWhoTerminated;
        this.terminatedBecause = terminatedBecause;
    }
    
    public Class<? extends MicroService> getServiceWhoTerminated() {
        return serviceWhoTerminated;
    }
    public String getTerminatedBecause() {
        return terminatedBecause;
    }
}
