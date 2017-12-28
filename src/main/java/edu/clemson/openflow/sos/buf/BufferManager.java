package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.rest.IncomingRequestHandler;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class BufferManager {
    private static final Logger log = LoggerFactory.getLogger(BufferManager.class);
    private ArrayList<Buffer> buffers = new ArrayList<>();


    private Buffer getBuffer(IncomingRequestMapper request) { // we will use client IP + client port to decide buffer
        for (Buffer buffer: buffers
             ) {
            if (buffer.equals(new Buffer(request))) {
                return buffer;
            }
        }
        return null;
    }
    public synchronized Buffer addBuffer(IncomingRequestMapper request, Object callBackHandler) {
       Buffer buffer = getBuffer(request);
        if (buffer == null) {
            buffer = new Buffer(request, callBackHandler);
            log.debug("Setting up new buffer for client {} : port {}",
                    request.getRequest().getClientIP(),
                    request.getRequest().getClientPort());
            buffers.add(buffer);
        }
      return buffer;
    }

}
