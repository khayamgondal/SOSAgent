package edu.clemson.openflow.sos;

import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.rest.RestServer;
import edu.clemson.openflow.sos.rest.TrafficHandlerTest;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MainTest {

    @Test
    public void main() {
        RestServer restServer = new RestServer();
        assertEquals(restServer.startComponent(), true);

        HostServer hostServer = new HostServer();
        assertEquals(hostServer.start(), true);

        AgentServer agentServer = new AgentServer();
        assertEquals(agentServer.start(), true);


        TrafficHandlerTest trafficHandlerTest = new TrafficHandlerTest();
        trafficHandlerTest.sendRestRequest();

        assertEquals(restServer.stopComponent(), true);
        assertEquals(hostServer.stop(), true);
        assertEquals(agentServer.stop(), true);
    }

}