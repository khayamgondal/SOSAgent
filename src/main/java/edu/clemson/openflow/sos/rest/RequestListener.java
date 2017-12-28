package edu.clemson.openflow.sos.rest;

import java.util.EventListener;

public interface RequestListener extends EventListener {
    void newIncomingRequest(RequestMapper request);
}
