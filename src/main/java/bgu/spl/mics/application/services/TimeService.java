package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    int currentTick, interval, duration;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.interval = TickTime;
        this.duration = Duration;
        this.currentTick = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (tickBroadcast) -> {
            if (currentTick == duration + 1) {
                System.out.println("end");
                sendBroadcast(new TerminatedBroadcast(TimeService.class, "The time has reached the Duration limit."));
                this.terminate();
            }
            else if (currentTick < duration + 1) {
                currentTick++;
                sendBroadcast(new TickBroadcast(currentTick));
            }
            try {
                Thread.sleep(interval*500);
            } catch (InterruptedException e) {
                //
            }
            
        });
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getServiceWhoTerminated() == FusionSlamService.class) {
                this.terminate();
            }
        });
        
        // Send the first tick to start the simulation
        sendBroadcast(new TickBroadcast(1));
        currentTick++;
    }
}
