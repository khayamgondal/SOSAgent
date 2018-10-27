package edu.clemson.openflow.sos.agent.blocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class BAgentServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BAgentServerHandler.class);
    private Socket socket;

    public BAgentServerHandler(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        log.info("{} connected", socket.getInetAddress().getHostAddress());
    }
}
