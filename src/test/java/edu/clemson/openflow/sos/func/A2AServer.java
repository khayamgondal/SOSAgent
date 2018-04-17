package edu.clemson.openflow.sos.func;

import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.rest.RestServer;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static junit.framework.TestCase.assertEquals;

public class A2AServer {

    private ServerSocket setupServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    @Test
    public void main() {

        RestServer restServer = new RestServer();
        assertEquals(restServer.startComponent(), true);

        AgentServer agentServer = new AgentServer();
        assertEquals(agentServer.start(), true);

        try {
            ServerSocket listener = setupServerSocket(5001);
            Socket socket = listener.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}