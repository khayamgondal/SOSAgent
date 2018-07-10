package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.agent.AgentClient;
import edu.clemson.openflow.sos.agent.AgentToHost;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.stats.StatCollector;
import edu.clemson.openflow.sos.utils.Utils;
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
    private static final int MAX_SEQ = 1000; //Integer.MAX_VALUE;

    private static final int MAX_BUF = 99000;

    private OrderedPacketInitiator orderedPacketInitiator;
    private RequestTemplateWrapper request;


    private int bufCount = 0;

    //TODO: Look into Google's ConcurrentHashMap	https://github.com/google/guava/wiki/NewCollectionTypesExplained
    private HashMap<Integer, ByteBuf> packetHolder;
    private HashMap<Integer, Boolean> status;

    public Buffer() {
      //  orderedPacketInitiator = new OrderedPacketInitiator();
        status = new HashMap<>();
        packetHolder = new HashMap<>();
    }

    public Buffer(RequestTemplateWrapper request) {
        clientIP = request.getRequest().getClientIP();
        clientPort = request.getRequest().getClientPort();
        this.request = request;

        if (Utils.configFile != null)
            bufferSize = Integer.parseInt(Utils.configFile.getProperty("buffer_size").replaceAll("[\\D]", ""));
        else bufferSize = MAX_BUF;

        status = new HashMap<>(request.getRequest().getBufferSize());
        packetHolder = new HashMap<>(request.getRequest().getBufferSize());

    }

    public int getClientPort() {
        return clientPort;
    }

    public void setOrderedPacketInitiator(OrderedPacketInitiator orderedPacketInitiator) {
        this.orderedPacketInitiator = orderedPacketInitiator;
    }

    public Buffer(RequestTemplateWrapper request, Object callBackHandler) {
        clientIP = request.getRequest().getClientIP();
        clientPort = request.getRequest().getClientPort();

        if (Utils.configFile != null)
            bufferSize = Integer.parseInt(Utils.configFile.getProperty("buffer_size").replaceAll("[\\D]", ""));
        else bufferSize = MAX_BUF;

        status = new HashMap<>(request.getRequest().getBufferSize());
        packetHolder = new HashMap<>(request.getRequest().getBufferSize());

        // TODO: remove the below section and make two setListerners()..
        // However this starts issues when we write to channel...
        // may be it notifies both AgentClient & AgentToHost... need to lookup
        if (callBackHandler != null) {
            orderedPacketInitiator = new OrderedPacketInitiator();
            try {
                orderedPacketInitiator.addListener((AgentClient) callBackHandler);
            } catch (ClassCastException e) {
                orderedPacketInitiator.addListener((AgentToHost) callBackHandler);
            }
        }
    }

    public synchronized void incomingPacket(ByteBuf data) {
        processPacket(data);
        //sendWithoutBuffering(data);
        //   dropData(data);
         //processDontSend(data);
    }

    public HashMap<Integer, Boolean> getStatus() {
        return status;
    }

    public HashMap<Integer, ByteBuf> getPacketHolder() {
        return packetHolder;
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
                // log.info("Sending {}", bufferIndex);
                if (sendData(packetHolder.get(bufferIndex))) {
                    status.put(bufferIndex, false);
                    log.debug("Sending from buffer to Host seq no. {}", expecting);
                    // log.info("Sending from buffer {}", expecting );
                    StatCollector.getStatCollector().bufferIndexRemoved();
                    expecting++;
                } else {
                    //log.error("Sending is blocked");
                }
            } else break;
        }
    }

    public void flushBuffer() {
        while (true) {
            for (int i = 0; i < status.size(); i++) {
                if (status.get(i) != null && status.get(i)) {
                    if (sendData(packetHolder.get(i)))
                        status.put(i, true);
                }

            }
        }
    }

    private void sendBufferDrop() {
        {
            while (true) { //also check our buffer. do we have some unsent packets there too.
                int bufferIndex = offSet(expecting);

                if (status.get(bufferIndex) != null && status.get(bufferIndex)) {
                    // log.info("Sending {}", bufferIndex);
                    bufCount--;
                    dropData(packetHolder.get(bufferIndex));
                    status.put(bufferIndex, false);
                    log.debug("Sending from buffer to Host seq no. {}", expecting);
                    //         log.info("Sending from buffer {}", expecting );

                    expecting++;
                } else break;
            }
        }
    }

    //TODO: Recheck the logic here.
    private void processPacket(ByteBuf data) {
        try {
            if (expecting == MAX_SEQ) expecting = 0;
            log.debug("Waiting for {}", expecting);
            int currentSeqNo = data.getInt(0); //get seq. no from incoming packet
            //TODO: may be use data.slice(0, 4) ??
            if (currentSeqNo == expecting) {
                //    log.info("Sending {}", currentSeqNo);
                if (sendData(data)) {
                    log.debug("Sending direclty to Host seq no: {} ", expecting);
                    //log.info("Sending Directly {}", currentSeqNo );
                    expecting++;
                    sendBuffer();
                } else {
                    putInBuffer(currentSeqNo, data); // failed to send, put in buffer
                }
            } else putInBuffer(currentSeqNo, data);
        } catch (IndexOutOfBoundsException exception) {
            // When client is done sending, agent on the otherside will send an empty bytebuf once that bytebuf is sent, it will close the channel.
            //      reason for sending this empty bytebuf is so we can findout once agent have successfully sent all the packets.
            //     But on the receiving agent side, it is not expecting empty packets and tries to use first 4 bytes as seq no. and due to an empty packet it
            // throws indexoutofbound exception. So i am just catching that exception here and not doing anything.
        }

    }

    public void processDontSend(ByteBuf data) {
        {
            try {
                if (expecting == MAX_SEQ) expecting = 0;
                log.debug("Waiting for {}", expecting);

                int currentSeqNo = data.getInt(0); //get seq. no from incoming packet
                //TODO: may be use data.slice(0, 4) ??
                //   log.info("buf used {}", bufCount);
                if (currentSeqNo == expecting) {
                    //    log.info("Sending {}", currentSeqNo);
                    dropData(data);
                    log.debug("Sending direclty to Host seq no: {} ", expecting);
                    //log.info("Sending Directly {}", currentSeqNo );

                    // check how much we have in buffer
                    expecting++;
                    sendBufferDrop();

                } else putInBufferAndDrop(currentSeqNo, data);
            } catch (IndexOutOfBoundsException exception) {
                // When client is done sending, agent on the otherside will send an empty bytebuf once that bytebuf is sent, it will close the channel.
                //      reason for sending this empty bytebuf is so we can findout once agent have successfully sent all the packets.
                //     But on the receiving agent side, it is not expecting empty packets and tries to use first 4 bytes as seq no. and due to an empty packet it
                // throws indexoutofbound exception. So i am just catching that exception here and not doing anything.
            }

        }

    }

    public void dropData(ByteBuf data) {
        System.out.println(data.getInt(0));
        data.release();
    }

    private void sendWithoutBuffering(ByteBuf data) {
        if (!sendData(data))
            dropData(data);
    }

    private void putInBuffer(int seqNo, ByteBuf data) {
        int bufferIndex = offSet(seqNo);

        if (status.get(bufferIndex) == null || !status.get(bufferIndex)) {
            bufCount++;
            packetHolder.put(bufferIndex, data);
            status.put(bufferIndex, true);
            log.debug("Putting seq no. {} in buffer on index {}", seqNo, bufferIndex);
            // log.info("BUffering {}", currentSeqNo );
            StatCollector.getStatCollector().bufferIndexUsed();
            sendBuffer();
        } else {
            //log.error("Receiving buffer index {} have unsent data dropping seq {}", bufferIndex, seqNo); //something wrong here... need to fix
            //ReferenceCountUtil.release(data);
            data.release();
            StatCollector.getStatCollector().packetDropped();
        }
    }

    private void putInBufferAndDrop(int seqNo, ByteBuf data) {
        int bufferIndex = offSet(seqNo);

        if (status.get(bufferIndex) == null || !status.get(bufferIndex)) { //for now just override previous buf loc
            bufCount++;
            packetHolder.put(bufferIndex, data);
            status.put(bufferIndex, true);
            //     log.info("Putting seq no. {} in buffer on index {}", seqNo, bufferIndex);
            // log.info("BUffering {}", currentSeqNo );
            sendBufferDrop();
        } else {
            log.error("Receiving buffer index {} have unsent data dropping seq {}", bufferIndex, seqNo); //something wrong here... need to fix
            //ReferenceCountUtil.release(data);
            data.release();
        }
    }


    private boolean sendData(ByteBuf data) {
        //System.out.println(data.getInt(0));
        if (orderedPacketInitiator != null)
        return orderedPacketInitiator.orderedPacket(data.slice(4, data.capacity()-4 )); //notify the listener
        else {
            log.error("packet listener is null... ");
            return false;
        }
      //  else return orderedPacketInitiator.orderedPacket(data); //TODO: change AgentServer to use this kinda logic

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
