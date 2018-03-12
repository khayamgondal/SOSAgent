package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.agent.AgentClient;
import edu.clemson.openflow.sos.agent.AgentToHost;
import edu.clemson.openflow.sos.rest.RequestMapper;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Buffer {

    private static final Logger log = LoggerFactory.getLogger(Buffer.class);

    private String clientIP; //remove these
    private int clientPort; //remove these
    private RequestMapper request;

    private int lastSent = -1;
    private int expecting = 0;
    private int sendFrom = 0;
    private int sendTill = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;
    private OrderedPacketInitiator orderedPacketInitiator;

    private HashMap<Integer, ByteBuf> bufs = new HashMap<>(1000); // to improve performance
    private HashMap<Integer, Boolean> status = new HashMap<>(1000); // to improve performance

    public Buffer() {
        orderedPacketInitiator = new OrderedPacketInitiator();
    }

    public Buffer(RequestMapper request) {
        this.clientIP = request.getRequest().getClientIP();
        this.clientPort = request.getRequest().getClientPort();
        this.request = request;
    }

    public Buffer(RequestMapper request, Object callBackHandler) {
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

    public synchronized void incomingPacket(ByteBuf data) { // need to check performance of this method
        if (expecting == MAX_SEQ) expecting = 0;
        log.info("DDDDDDDDD {}", expecting);
        int currentSeqNo = data.getInt(0);
        if (currentSeqNo == expecting) {
            sendData(data);
            log.debug("Sending to Host seq no: {} ", expecting);
            // check how much we have in buffer
            expecting++; //TODO: circular increment
                while (true) {
                    if (status.get(expecting) != null && status.get(expecting) != false) {
                        sendData(data);
                        status.put(expecting, false);
                        log.debug("Sending to Host seq no. {}", expecting);
                        expecting++; //TODO: circular loop
                    } else break;
                }

        } else {
            if (status.get(currentSeqNo) == null ||status.get(currentSeqNo) == false) {
                bufs.put(currentSeqNo, data);
                status.put(currentSeqNo, true);
                log.debug("Putting seq no. {} in buffer", currentSeqNo);
            }
            else log.error("Still unsent packets in buffer.. droping this packet");
        }
    }

    private void sendData(ByteBuf data) {
       // if (orderedPacketInitiator !=null)
        //TODO: remove request from here
            orderedPacketInitiator.orderedPacket(data, request); //notify the listener
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
