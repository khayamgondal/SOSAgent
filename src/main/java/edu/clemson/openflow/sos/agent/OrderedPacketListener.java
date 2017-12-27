package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import io.netty.buffer.ByteBuf;

public interface OrderedPacketListener {
    void orderedPacket(ByteBuf packet, IncomingRequestMapper request);

}
