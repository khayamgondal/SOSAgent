package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.agent.IncomingRequestListener;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;

public class PacketFilter  {

    private IncomingRequestMapper request;

    public PacketFilter(IncomingRequestMapper request) {
        this.request = request;
    }

    public void packetToFilter(byte[] packet) {

    }


}
