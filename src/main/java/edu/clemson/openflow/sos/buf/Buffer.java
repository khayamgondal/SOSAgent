package edu.clemson.openflow.sos.buf;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Buffer {

    private String clientIP;
    private int clientPort;

    private int lastSent = -1;
    private int expecting = 0;
    private int sendFrom = 0;
    private int sendTill = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;

    //private int[] buffer = new int[100 * PACKET_SIZE];
    ByteBuffer byteBuf = ByteBuffer.allocate(16000); //
    HashMap<Integer, Integer> offSet = new HashMap<>(100); // to improve performance
    private int lastPacketSize = 0;

    //private boolean[] status = new boolean[100];

    public Buffer(String clientIP, int clientPort) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
    }

    public void incomingPacket(ByteBuf data) { // need to check perforamance of this method
        if (data.getInt(0) == expecting) {
            // check how much we have in buffer
            for (int i = expecting + 1; i < expecting; i = (i+1)%Integer.MAX_VALUE) {

              //  if (status[i] == false) {
                    sendTill = i - 1;
                    sendData(data, sendFrom, sendTill);
                    expecting = sendTill + 1;
                    break;
            //    } else {
             //       sendFrom = expecting + 1;

            //    }
            }
        } else { ByteBuf bb;
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
