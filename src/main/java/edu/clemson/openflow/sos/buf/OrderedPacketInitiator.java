package edu.clemson.openflow.sos.buf;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

public class OrderedPacketInitiator {
    private ArrayList<OrderedPacketListener> listeners = new ArrayList<>();

    public void addListener(OrderedPacketListener toAdd) {
        listeners.add(toAdd);
    }
    public void orderedPacket(ByteBuf packet) {
        for (OrderedPacketListener listener: listeners
             ) {
            if (listener!=null) listener.orderedPacket(packet);
        }
    }
}
