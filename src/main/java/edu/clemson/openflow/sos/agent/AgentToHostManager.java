package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.RequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class AgentToHostManager {
    private static final Logger log = LoggerFactory.getLogger(AgentToHostManager.class);
    private ArrayList<AgentToHost> hosts = new ArrayList<>();

    public AgentToHostManager() {
        log.debug("Setting up Agent2Host manager");
    }

    private AgentToHost getAgentToHost(RequestMapper request) {
        for (AgentToHost host : hosts
                ) {
            if (host.equals(request)) return host;
        }
        return null;
    }

    public synchronized AgentToHost addAgentToHost(RequestMapper request) {
        AgentToHost host = getAgentToHost(request);
        if (host == null) {
            log.debug("Creating new host handler for server {} port {}",
                    request.getRequest().getServerIP(),
                    request.getRequest().getServerPort());
            host = new AgentToHost(request);
            hosts.add(host);

        } else log.debug("Already exists host handler for server {} port {}",
                request.getRequest().getServerIP(),
                request.getRequest().getServerPort());

        return host;
    }

    public void removeAgentToHost(AgentToHost host) {
        if (hosts.contains(host)) hosts.remove(host);
    }
}
