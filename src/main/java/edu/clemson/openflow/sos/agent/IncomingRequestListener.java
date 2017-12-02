package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.buf.PacketBuffer;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;

import java.util.EventListener;

public interface IncomingRequestListener extends EventListener {
    public void newIncomingRequest(IncomingRequestMapper request, PacketBuffer packetBuffer);
}
