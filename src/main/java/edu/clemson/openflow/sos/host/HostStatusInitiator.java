package edu.clemson.openflow.sos.host;

import java.util.ArrayList;
import java.util.List;

public class HostStatusInitiator {
    private List<HostStatusListener> listeners = new ArrayList<>();

    public void addListener(HostStatusListener listener) {
        listeners.add(listener);
    }
    public void hostStatusChanged(HostStatusListener.HostStatus status) {
        for (HostStatusListener listener: listeners
                ) {
            listener.HostStatusChanged(status);
        }
    }
}
