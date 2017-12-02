package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author Khayam Gondal    kanjam@g.clemson.edu
 * This class will buffer data for each connected client.
 */
public class PacketBuffer {
    private static final Logger log = LoggerFactory.getLogger(PacketBuffer.class);

    private ByteBuffer byteBuffer;
    private int currentSize;
    private int offSet;
    //private BufferStatusListener bufferStatusListener;
    private int incomingPacketSize;
    private static final int BUFFER_SIZE = 16 * 1024;
    private IncomingRequestMapper request;

    public PacketBuffer(IncomingRequestMapper request) {
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            log.debug("Buffer for client {}:{} is created of size {}",
                    request.getRequest().getClientIP(), request.getRequest().getClientPort(), request.getRequest().getQueueCapacity());
        }
        if (request == null) this.request = request;

        //bufferStatusListener = new Multiplexer();
        //bufferStatusListener.BufferInitialized(request, byteBuffer); //notify the listeners
        offSet = 0;
    }

    public IncomingRequestMapper getRequest() {
        return request;
    }

    /*
        write the packet in the buffer. if buffer is full. than start rewriting from the start.
         */
    public boolean putPacket(Object data) {
        byte[] bytes = (byte[]) data;
        if (offSet + bytes.length <= byteBuffer.capacity()) {
            log.debug("Byte buffer full.. will start rewriting from the start");
            offSet = 0;
        }
            byteBuffer.put(bytes, offSet, bytes.length);
            offSet += bytes.length;
            log.debug("Wrote {} bytes into byte buffer", bytes.length);
            return true;
    }


    public int getCurrentSize() {
        return currentSize;
    }
}
