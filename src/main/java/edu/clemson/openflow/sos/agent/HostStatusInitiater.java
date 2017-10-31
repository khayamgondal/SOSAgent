package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.RequestParser;

import java.util.ArrayList;
import java.util.List;

public class HostStatusInitiater {
    private List<HostStatusListener> listeners = new ArrayList<HostStatusListener>();

    public void addListener(HostStatusListener toAdd) {
        listeners.add(toAdd);
    }

    public void hostConnected(RequestParser request) {
        for (HostStatusListener listener : listeners)
            listener.hostConnected(request);
    }

    public void packetArrived(String hostIP, int hostPort, Object msg) {
        for (HostStatusListener listener : listeners)
            listener.packetArrived(hostIP, hostPort, msg);
    }

}
