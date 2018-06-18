package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class BufferManager {
    private static final Logger log = LoggerFactory.getLogger(BufferManager.class);
    private ArrayList<Buffer> buffers = new ArrayList<>();


    private Buffer getBuffer(RequestTemplateWrapper request) { // we will use client IP + client port to decide buffer
        for (Buffer buffer: buffers
             ) {
            if (buffer.equals(new Buffer(request))) {
                return buffer;
            }
        }
        return null;
    }
    public synchronized Buffer addBuffer(RequestTemplateWrapper request, Object callBackHandler) {
       Buffer buffer = getBuffer(request);
        if (buffer == null) {
            buffer = new Buffer(request, callBackHandler);
            log.info("Setting up new buffer for client {} : port {}",
                    request.getRequest().getClientIP(),
                    request.getRequest().getClientPort());
            buffers.add(buffer);
        }
      return buffer;
    }

    public void removeBuffer(Buffer buffer) {
        if(buffers.contains(buffer)) buffers.remove(buffer);
    }
}
