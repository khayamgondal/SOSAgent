package edu.clemson.openflow.sos.buf;

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

    //private int[] buffer = new int[100 * PACKET_SIZE];
    ByteBuffer byteBuf = ByteBuffer.allocate(16000); //


    private HashMap<Integer, ByteBuf> bufs = new HashMap<>(1000); // to improve performance
    private HashMap<Integer, Boolean> status = new HashMap<>(1000); // to improve performance

    private int lastPacketSize = 0;

    //private boolean[] status = new boolean[100];

    public Buffer(String clientIP, int clientPort) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
    }

    public void incomingPacket(ByteBuf data) { // need to check perforamance of this method
        int currentSeqNo = data.getInt(0);
        if (currentSeqNo == expecting) {
            // send this index
            log.debug("Sending seq no: {}", expecting);
            // check how much we have in buffer
            expecting++;
            for (int i = expecting ; i < expecting - 1; i = (i+1)%Integer.MAX_VALUE) {
                if (status.get(i) != null) {
                    //send this index
                    log.debug("Sending seq no: {}", expecting);
                    expecting ++;
                }
                else break;
            //    } else {
             //       sendFrom = expecting + 1;

            //    }
            }
        } else {
            bufs.put(currentSeqNo, data);
            status.put(currentSeqNo, true);
            log.debug("Wrote packet no, {} ,in buffer", currentSeqNo);
            //bufferMap.put(data.getInt(0), data);
           // byteBuf.putInt(data.getInt(0), data);
            //write into byte array
            /*for (int i = data[0]; i < data.length; i++) {
                buffer[i] = data[i];
            }
            status[data[0]] = true; // also write it in status*/
        }
    }

    private void sendData(ByteBuf data, int sendFrom, int sentTill) {

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
