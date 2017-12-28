package edu.clemson.openflow.sos.agent;

import java.util.ArrayList;
import java.util.List;

public class HostPacketInitiator {
    private List<HostPacketListener> listeners = new ArrayList<>();

    public void addListener(HostPacketListener listener) {
        listeners.add(listener);
    }
    public void hostPacket(byte[] packet) {
        for (HostPacketListener listener: listeners
             ) {
            listener.hostPacket(packet);
        }
    }
}
