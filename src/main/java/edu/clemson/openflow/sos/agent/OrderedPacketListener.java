package edu.clemson.openflow.sos.agent;

import io.netty.buffer.ByteBuf;

public interface OrderedPacketListener {
    void orderedPacket(ByteBuf packet);

}
