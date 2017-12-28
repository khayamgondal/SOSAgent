package edu.clemson.openflow.sos.agent;

import io.netty.buffer.ByteBuf;

public interface HostPacketListener {
    void hostPacket(byte[] packet);
}
