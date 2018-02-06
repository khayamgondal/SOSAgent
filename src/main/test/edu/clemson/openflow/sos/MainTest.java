package edu.clemson.openflow.sos;

import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.rest.RestServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void main() {
        RestServer restServer = new RestServer();
        assertEquals(restServer.startComponent(), true);

        HostServer hostServer = new HostServer();
        assertEquals(hostServer.start(), true);

        AgentServer agentServer = new AgentServer();
        assertEquals(agentServer.start(), true);

        assertEquals(restServer.stopComponent(), true);
        assertEquals(hostServer.stop(), true);
        assertEquals(agentServer.stop(), true);
    }

}