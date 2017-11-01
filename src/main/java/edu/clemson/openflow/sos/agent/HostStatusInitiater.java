package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.RequestParser;

import java.util.ArrayList;
import java.util.List;

public class HostStatusInitiater {
    private List<HostStatusListener> listeners = new ArrayList<HostStatusListener>();

    public void addListener(HostStatusListener toAdd) {
        listeners.add(toAdd);
    }

    public void hostConnected(RequestParser request, HostStatusInitiater callBackhostStatusInitiater) {
        for (HostStatusListener listener : listeners)
            listener.hostConnected(request, callBackhostStatusInitiater);
    }

    public void packetArrived(Object msg) {
        for (HostStatusListener listener : listeners)
            listener.packetArrived(msg);
    }

}
