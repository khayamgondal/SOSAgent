package edu.clemson.openflow.sos.manager;

import edu.clemson.openflow.sos.exceptions.RequestNotFoundException;
import edu.clemson.openflow.sos.rest.AgentPortMapper;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;

import java.util.ArrayList;

public enum PortManager {
    INSTANCE;
    private ArrayList<AgentPortMapper> incomingAgentPorts = new ArrayList<>();

    public void addToPool(AgentPortMapper ports) {
        incomingAgentPorts.add(ports);
    }

    public AgentPortMapper getPorts(String remoteAgentIP, int remoteAgentPort) {
        for (AgentPortMapper incomingAgentPort : incomingAgentPorts
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

}
