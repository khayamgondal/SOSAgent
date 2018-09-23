package edu.clemson.openflow.sos.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HostStatusInitiator {
    private static final Logger log = LoggerFactory.getLogger(HostClient.class);
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
