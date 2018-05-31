package edu.clemson.openflow.sos.buf;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

public class OrderedPacketInitiator {
    private ArrayList<OrderedPacketListener> listeners = new ArrayList<>();

    public void addListener(OrderedPacketListener toAdd) {
        listeners.add(toAdd);
    }
    public boolean orderedPacket(ByteBuf packet) {
        for (OrderedPacketListener listener: listeners
             ) {
            if (listener!=null) return listener.orderedPacket(packet);
        }
        return false;
    }
}
