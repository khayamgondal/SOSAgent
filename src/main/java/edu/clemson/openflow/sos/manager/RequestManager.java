package edu.clemson.openflow.sos.manager;

import edu.clemson.openflow.sos.exceptions.RequestNotFoundException;
import edu.clemson.openflow.sos.rest.RequestParser;

import java.util.ArrayList;

public enum RequestManager {
    INSTANCE;
    private ArrayList<RequestParser> incomingRequests = new ArrayList<>();

    public void addToPool(RequestParser request) {
        incomingRequests.add(request);
    }

    public ArrayList<RequestParser> getRequests() {
        return incomingRequests;
    }

    private boolean isClientAgentRequest(String IP, int port) {
        for (RequestParser request : incomingRequests) {
            if ((request.getClientIP().equals(IP) &&
                    request.getClientPort() == port))
                return true;
        }
        return false;
    }

    private RequestParser getClientAgentRequest(String IP, int port) {
        for (RequestParser request : incomingRequests) {
            if ((request.getClientIP().equals(IP) &&
                    request.getClientPort() == port))
                return request;
        }
        return null;
    }

    private RequestParser getServerAgentRequest(String clientAgentIP) {
        for (RequestParser request : incomingRequests) {
            if (request.getClientAgentIP().equals(clientAgentIP)) return request;
        }
        return null;
    }
    public RequestParser getRequest(String IP, int port, boolean isClientAgent)
            throws RequestNotFoundException {
        if (isClientAgent) return getClientAgentRequest(IP, port);
        else return  getServerAgentRequest(IP);
    }
}
