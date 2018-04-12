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

    private String clientIP;
    private int clientPort;

    private int bufferSize;
    private int expecting = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;


    private OrderedPacketInitiator orderedPacketInitiator;

    private HashMap<Integer, ByteBuf> packetHolder;
    private HashMap<Integer, Boolean> status;

    public Buffer() {
        orderedPacketInitiator = new OrderedPacketInitiator();
        status = new HashMap<>();
        packetHolder = new HashMap<>();
    }

    public Buffer(RequestMapper request) {
        clientIP = request.getRequest().getClientIP();
        clientPort = request.getRequest().getClientPort();

        bufferSize = request.getRequest().getBufferSize();

        status = new HashMap<>(request.getRequest().getBufferSize());
        packetHolder = new HashMap<>(request.getRequest().getBufferSize());

    }

    /*
        packetHolder and status are initialized with BufferSize received in request from controller.
        HashMap is capable of auto increasing the capacity if it hits the limit but its expensive on resources cause JVM
        need to create a new HashMap and than copy all of values from old map to new one.
        If you have specified really small buffersize and you are seeing alot of CPU usage, this could be the issue
     */
    public Buffer(RequestMapper request, Object callBackHandler) {
        clientIP = request.getRequest().getClientIP();
        clientPort = request.getRequest().getClientPort();

        status = new HashMap<>(request.getRequest().getBufferSize());
        packetHolder = new HashMap<>(request.getRequest().getBufferSize());

        if (callBackHandler != null) {
            orderedPacketInitiator = new OrderedPacketInitiator();
            orderedPacketInitiator.addListener((AgentToHost) callBackHandler);
        }
    }

    public void setListener(Object listener) {
        orderedPacketInitiator.addListener((AgentClient) listener);
    }

    private int offSet(int seq) {
        return seq % bufferSize;
    }

    private void sendBuffer() {
        while (true) { //also check our buffer. do we have some unsent packets there too.
            int bufferIndex = offSet(expecting);

            if (status.get(bufferIndex) != null && status.get(bufferIndex)) {
                sendData(packetHolder.get(bufferIndex));
                status.put(bufferIndex, false);
                log.debug("Sending to Host seq no. {}", expecting);
                //         log.info("Sending from buffer {}", expecting );

                expecting++;
            } else break;
        }
    }
    public void incomingPacket(ByteBuf data) {
        if (expecting == MAX_SEQ) expecting = 0;
        int currentSeqNo = data.getInt(0); //get seq. no from incoming packet
        if (currentSeqNo == expecting) {
            sendData(data);
            log.debug("Sending to Host seq no: {} ", expecting);
            //log.info("Sending Directly {}", currentSeqNo );

            // check how much we have in buffer
            expecting++;
            sendBuffer();

        } else {
            if (status.get(currentSeqNo) == null || !status.get(currentSeqNo)) {
                packetHolder.put(currentSeqNo, data);
                status.put(currentSeqNo, true);
                log.debug("Putting seq no. {} in buffer", currentSeqNo);
                // log.info("BUffering {}", currentSeqNo );
                sendBuffer();
            } else
                log.error("Still unsent packets in buffer.. droping seq. {}", currentSeqNo); //something wrong here... need to fix
        }
    }

    /*public synchronized void incomingPacket(ByteBuf data) { // need to check performance of this method
        //sendData(data);
        //log.info("SIZE   {}", data.capacity());
        if (expecting == MAX_SEQ) expecting = 0;
        // log.info("Expecting {}", expecting);
        int currentSeqNo = data.getInt(0); //get seq. no from incoming packet
        if (currentSeqNo == expecting) {
            sendData(data);
            log.debug("Sending to Host seq no: {} ", expecting);
            //log.info("Sending Directly {}", currentSeqNo );

            // check how much we have in buffer
            expecting++;
            while (true) { //also check our buffer. do we have some unsent packets there too.
                if (status.get(expecting) != null && status.get(expecting)) {
                    sendData(packetHolder.get(expecting));
                    status.put(expecting, false);
                    log.debug("Sending to Host seq no. {}", expecting);
                    //         log.info("Sending from buffer {}", expecting );

                    expecting++;
                } else break;
            }

        } else {
            if (status.get(currentSeqNo) == null || !status.get(currentSeqNo)) {
                packetHolder.put(currentSeqNo, data);
                status.put(currentSeqNo, true);
                log.debug("Putting seq no. {} in buffer", currentSeqNo);
                // log.info("BUffering {}", currentSeqNo );
                while (true) { //also check our buffer. do we have some unsent packets there too.
                    if (status.get(expecting) != null && status.get(expecting)) {
                        sendData(packetHolder.get(expecting));
                        status.put(expecting, false);
                        log.debug("Sending to Host seq no. {}", expecting);
                        //         log.info("Sending from buffer {}", expecting );

                        expecting++;
                    } else break;
                }
            } else log.error("Still unsent packets in buffer.. droping seq. {}", currentSeqNo); //something wrong here... need to fix
        }
    }*/

    private void sendData(ByteBuf data) {
        orderedPacketInitiator.orderedPacket(data); //notify the listener
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
