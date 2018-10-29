package edu.clemson.openflow.sos.agent;

public abstract class SendingStrategy {

     protected int totalChannels;
     protected int currentChannel = -1;

    public SendingStrategy(int totalChannels){
        this.totalChannels = totalChannels;
    }

    abstract public int channelToSendOn();

    public int getCurrentChannel() {
        return currentChannel;
    }

    public int getTotalChannels() {
        return totalChannels;
    }
}
