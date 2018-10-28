package edu.clemson.openflow.sos.agent.blocking;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class BAgentClient {
    private String remoteAgentIP;
    private int remoteAgentPort;
    private int totalSocks;

    public BAgentClient(String remoteAgentIP, int remoteAgentPort, int totalSocks) {
        this.remoteAgentIP = remoteAgentIP;
        this.remoteAgentPort = remoteAgentPort;
        this.totalSocks = totalSocks;
    }

    public List<Socket> connectSocks() {
        List<Socket> clientSockets = new ArrayList<>();
        for (int i = 0; i < totalSocks; i++) {
            try {
                Socket s = new Socket(remoteAgentIP, remoteAgentPort);
                clientSockets.add(s);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return clientSockets;
    }
}
