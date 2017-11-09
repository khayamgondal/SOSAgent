package edu.clemson.openflow.sos.manager;

import edu.clemson.openflow.sos.exceptions.RequestNotFoundException;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;

import java.util.ArrayList;

public enum RequestManager {
    INSTANCE;
    private ArrayList<ControllerRequestMapper> incomingRequests = new ArrayList<>();

    public void addToPool(ControllerRequestMapper request) {
        incomingRequests.add(request);
    }

    public ArrayList<ControllerRequestMapper> getRequests() {
        return incomingRequests;
    }

    private boolean isClientAgentRequest(String IP, int port) {
        for (ControllerRequestMapper request : incomingRequests) {
            if ((request.getClientIP().equals(IP) &&
                    request.getClientPort() == port))
                return true;
        }
        return false;
    }

    private ControllerRequestMapper getClientAgentRequest(String IP, int port) {
        for (ControllerRequestMapper request : incomingRequests) {
            if ((request.getClientIP().equals(IP) &&
                    request.getClientPort() == port))
                return request;
        }
        return null;
    }

    private ControllerRequestMapper getServerAgentRequest(String clientAgentIP) {
        for (ControllerRequestMapper request : incomingRequests) {
            if (request.getClientAgentIP().equals(clientAgentIP)) return request;
        }
        return null;
    }
    public ControllerRequestMapper getRequest(String IP, int port, boolean isClientAgent)
            throws RequestNotFoundException {
        if (isClientAgent) return getClientAgentRequest(IP, port);
        else return  getServerAgentRequest(IP);
    }
}
