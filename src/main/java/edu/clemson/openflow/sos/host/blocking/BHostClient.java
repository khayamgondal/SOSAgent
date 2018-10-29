package edu.clemson.openflow.sos.host.blocking;

import java.io.IOException;
import java.net.Socket;

public class BHostClient {
    private String remoteAgentIP;
    private int remoteAgentPort;

    public BHostClient(String remoteAgentIP, int remoteAgentPort) {
        this.remoteAgentIP = remoteAgentIP;
        this.remoteAgentPort = remoteAgentPort;
    }

    public Socket connectSocket() {
        try {
            return new Socket(remoteAgentIP, remoteAgentPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
