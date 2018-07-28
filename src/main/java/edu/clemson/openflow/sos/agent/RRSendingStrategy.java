package edu.clemson.openflow.sos.agent;

/**
 * Send in round robbin way
 */
public class RRSendingStrategy extends SendingStrategy {

    public RRSendingStrategy(int totalChannels) {
        super(totalChannels);
    }

    @Override
    public int channelToSendOn() {
        currentChannel = (currentChannel + 1) % totalChannels;
        return currentChannel;
    }


}
