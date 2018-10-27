package edu.clemson.openflow.sos;

import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.rest.RestServer;
import edu.clemson.openflow.sos.rest.TrafficHandlerTest;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MainTest {
    private static final int HOST_DATA_PORT = 9877;
    private static final int AGENT_DATA_PORT = 9878;
    @Test
    public void main() {
        RestServer restServer = new RestServer();
        assertEquals(restServer.startComponent(), true);

        HostServer hostServer = new HostServer();
        assertEquals(hostServer.start(HOST_DATA_PORT), true);

        AgentServer agentServer = new AgentServer();
        assertEquals(agentServer.start(AGENT_DATA_PORT), true);


        TrafficHandlerTest trafficHandlerTest = new TrafficHandlerTest();
        trafficHandlerTest.sendRestRequest();

        assertEquals(restServer.stopComponent(), true);
        assertEquals(hostServer.stop(), true);
        assertEquals(agentServer.stop(), true);
    }

}