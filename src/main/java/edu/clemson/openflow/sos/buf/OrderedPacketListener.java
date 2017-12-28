package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.rest.RequestMapper;
import io.netty.buffer.ByteBuf;

public interface OrderedPacketListener {
    void orderedPacket(ByteBuf packet, RequestMapper request);

}
