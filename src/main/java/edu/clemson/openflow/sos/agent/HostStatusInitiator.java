package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.ControllerRequestMapper;

import java.util.ArrayList;
import java.util.List;

public class HostStatusInitiator {
    private List<HostStatusListener> listeners = new ArrayList<HostStatusListener>();

    public void addListener(HostStatusListener toAdd) {
        listeners.add(toAdd);
    }

    public void hostConnected(ControllerRequestMapper request, Object callBackObject) {
        for (HostStatusListener listener : listeners)
            listener.hostConnected(request, callBackObject);
    }

    public void packetArrived(Object msg) {
        for (HostStatusListener listener : listeners)
            listener.packetArrived(msg);
    }

}
