package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.rest.ControllerRequestMapper;

import java.nio.ByteBuffer;
import java.util.EventListener;

/**
 * Listener for buffer events i.e. new client connected, buffer reached mini size, buffer is full
 */
public interface BufferStatusListener extends EventListener {

    public void BufferInitialized(ControllerRequestMapper request, ByteBuffer byteBuffer);

    /**
     * Minimum decided buffer is filled. You can start processing packets.
     */
    public void BufferedMinimum();

    /**
     * Buffer has new data available.
     */
    public void BufferRewriteStarted();
    public void BufferIsFull();
}
