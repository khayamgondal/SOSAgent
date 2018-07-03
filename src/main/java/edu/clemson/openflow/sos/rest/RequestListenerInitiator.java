package edu.clemson.openflow.sos.rest;

import java.util.ArrayList;
import java.util.List;

public class RequestListenerInitiator {
    List<RequestListener> requestListenerList = new ArrayList<>();

    public void addRequestListener(RequestListener listener) {
        requestListenerList.add(listener);
    }

}
