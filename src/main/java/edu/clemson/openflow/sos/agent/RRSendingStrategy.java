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
        if (currentChannel == totalChannels)
            return currentChannel = 1;
        else return ++currentChannel;
    }


}
