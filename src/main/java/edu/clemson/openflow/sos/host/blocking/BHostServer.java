package edu.clemson.openflow.sos.host.blocking;

import edu.clemson.openflow.sos.manager.ISocketServer;

import java.io.IOException;
import java.net.ServerSocket;

public class BHostServer implements ISocketServer {

    private boolean startSocket(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean start(int port) {
        return startSocket(port);
    }

    @Override
    public boolean stop() {
        return false;
    }
}
