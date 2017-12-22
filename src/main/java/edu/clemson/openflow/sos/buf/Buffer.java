package edu.clemson.openflow.sos.buf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Buffer {

    private String clientIP;
    private int clientPort;

    private int lastSent = -1;
    private int expecting = 0;
    private int sendFrom = 0;
    private int sentTill = 0;

    public Buffer(String clientIP, int clientPort) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
    }

    public void incomingPacket(byte[] data) {

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
