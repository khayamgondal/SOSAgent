package edu.clemson.openflow.sos.manager;

import edu.clemson.openflow.sos.rest.IncomingRequestMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum IncomingRequestManager {
    INSTANCE;
    private ArrayList<IncomingRequestMapper> incomingRequests = new ArrayList<>();

    public void addToPool(IncomingRequestMapper ports) {
        incomingRequests.add(ports);
    }

    public IncomingRequestMapper getRequest(String remoteAgentIP, int remoteAgentPort) {
        for (IncomingRequestMapper incomingAgentPort : incomingRequests
                ) {
            if (incomingAgentPort.getRequest().getClientAgentIP().equals(remoteAgentIP)) {
                for (Integer port : incomingAgentPort.getPorts()
                        ) {
                    if (port.equals(remoteAgentPort)) return incomingAgentPort;
                }
                return null;
            }
            return null;
        }
        return null;
    }
    public List<IncomingRequestMapper> getAllRequests() { return incomingRequests; }

    public Optional<IncomingRequestMapper> getRequestByPort(int assignedPort) {
        return getAllRequests().stream()
                .filter(p -> p.getPorts().contains(assignedPort)).findAny();
    }
}
