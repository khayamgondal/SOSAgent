package edu.clemson.openflow.sos.buf;

import io.netty.buffer.ByteBuf;

public interface OrderedPacketListener {
    void orderedPacket(ByteBuf packet);

}
