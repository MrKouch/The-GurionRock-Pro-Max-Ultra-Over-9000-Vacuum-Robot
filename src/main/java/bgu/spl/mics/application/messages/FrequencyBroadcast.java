package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class FrequencyBroadcast implements Broadcast {
    private int frequency;

    public FrequencyBroadcast(int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

}
