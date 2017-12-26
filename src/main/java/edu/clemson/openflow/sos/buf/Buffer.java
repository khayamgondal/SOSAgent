package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.agent.OrderedPacketInitiator;
import edu.clemson.openflow.sos.agent.netty.AgentServer;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Buffer {

    private static final Logger log = LoggerFactory.getLogger(Buffer.class);

    private String clientIP;
    private int clientPort;

    private int lastSent = -1;
    private int expecting = 0;
    private int sendFrom = 0;
    private int sendTill = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;
    private Object caller;
    private OrderedPacketInitiator orderedPacketInitiator;

    private HashMap<Integer, ByteBuf> bufs = new HashMap<>(1000); // to improve performance
    private HashMap<Integer, Boolean> status = new HashMap<>(1000); // to improve performance

    private int lastPacketSize = 0;

    //private boolean[] status = new boolean[100];

    public Buffer(String clientIP, int clientPort, Object caller) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        if (caller != null) orderedPacketInitiator.addListener((AgentServer.AgentServerHandler)caller);
    }

    public void incomingPacket(ByteBuf data) { // need to check performance of this method
        int currentSeqNo = data.getInt(0);
        if (currentSeqNo == expecting) {
            sendData(data);
            log.debug("Sending seq no: {}", expecting);
            // check how much we have in buffer
            expecting++; //TODO: circular increment
                while (true) {
                    if (status.get(expecting) != null && status.get(expecting) != false) {
                        sendData(data);
                        status.put(expecting, false);
                        log.debug("Sending seq no: {}", expecting);
                        expecting++;
                    } else break;
                }

        } else {
            bufs.put(currentSeqNo, data);
            status.put(currentSeqNo, true);
        }
    }

    private void sendData(ByteBuf data) {
        if (orderedPacketInitiator !=null) orderedPacketInitiator.orderedPacket(data); //notify the listener
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
