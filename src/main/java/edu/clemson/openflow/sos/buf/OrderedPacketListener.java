package edu.clemson.openflow.sos.buf;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;

public interface OrderedPacketListener {

   // void holders(HashMap<Integer, ByteBuf> packetHolder, HashMap<Integer, Boolean> status);
     boolean orderedPacket(ByteBuf packet);

}
