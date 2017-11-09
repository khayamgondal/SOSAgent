package edu.clemson.openflow.sos.buf;

import java.util.ArrayList;
import java.util.List;

public class BufferStatusInitiator {

    private List<BufferStatusListener> listeners = new ArrayList<BufferStatusListener>();

    private void addToList(BufferStatusListener listener) {
       listeners.add(listener);
    }



}
