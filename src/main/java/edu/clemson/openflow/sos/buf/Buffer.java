package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.agent.OrderedPacketInitiator;
import edu.clemson.openflow.sos.agent.netty.AgentClient;
import edu.clemson.openflow.sos.agent.netty.AgentServer;
import edu.clemson.openflow.sos.agent2host.AgentToHost;
import edu.clemson.openflow.sos.agent2host.AgentToHostManager;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Buffer {

    private static final Logger log = LoggerFactory.getLogger(Buffer.class);

    private String clientIP; //remove these
    private int clientPort; //remove these
    private IncomingRequestMapper request;

    private int lastSent = -1;
    private int expecting = 0;
    private int sendFrom = 0;
    private int sendTill = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;
 //   private Object caller;
    private OrderedPacketInitiator orderedPacketInitiator;

    private HashMap<Integer, ByteBuf> bufs = new HashMap<>(1000); // to improve performance
    private HashMap<Integer, Boolean> status = new HashMap<>(1000); // to improve performance

    private int lastPacketSize = 0;

    //private boolean[] status = new boolean[100];

    public Buffer() {
        orderedPacketInitiator = new OrderedPacketInitiator();
    }

    public Buffer(IncomingRequestMapper request) {
        this.clientIP = request.getRequest().getClientIP();
        this.clientPort = request.getRequest().getClientPort();
        this.request = request;
    }

    public Buffer(IncomingRequestMapper request, Object callBackHandler) {
        this.clientIP = request.getRequest().getClientIP();
        this.clientPort = request.getRequest().getClientPort();

        if (callBackHandler != null) {
            orderedPacketInitiator = new OrderedPacketInitiator();
            orderedPacketInitiator.addListener((AgentToHost)callBackHandler);
        }
    }

    public void setListener(Object listener) {
        orderedPacketInitiator.addListener((AgentClient)listener);
    }

    public void incomingPacket(ByteBuf data) { // need to check performance of this method
        int currentSeqNo = data.getInt(0);
        if (currentSeqNo == expecting) {
            sendData(data);
            log.debug("Sending seq no: back{}", expecting);
            // check how much we have in buffer
            expecting++; //TODO: circular increment
                while (true) {
                    if (status.get(expecting) != null && status.get(expecting) != false) {
                        sendData(data);
                        status.put(expecting, false);
                        log.debug("Sending seq no: back{}", expecting);
                        expecting++; //TODO: circular loop
                    } else break;
                }

        } else {
            bufs.put(currentSeqNo, data);
            status.put(currentSeqNo, true);
        }
    }

    private void sendData(ByteBuf data) {
        if (orderedPacketInitiator !=null) orderedPacketInitiator.orderedPacket(data, request); //notify the listener
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Buffer buffer = (Buffer) o;

        if (clientPort != buffer.clientPort) return false;
        return clientIP.equals(buffer.clientIP);
    }

    @Override
    public int hashCode() {
        int result = clientIP.hashCode();
        result = 31 * result + clientPort;
        return result;
    }
}
