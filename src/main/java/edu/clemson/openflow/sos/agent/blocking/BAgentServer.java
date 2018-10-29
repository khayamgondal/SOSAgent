package edu.clemson.openflow.sos.agent.blocking;

import edu.clemson.openflow.sos.Main;
import edu.clemson.openflow.sos.host.blocking.BHostClient;
import edu.clemson.openflow.sos.host.blocking.BHostServer;
import edu.clemson.openflow.sos.host.blocking.BHostServerHandler;
import edu.clemson.openflow.sos.manager.ISocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BAgentServer implements ISocketServer {
    private static final Logger log = LoggerFactory.getLogger(BAgentServer.class);
    private BHostClient bHostClient = new BHostClient("10.0.0.211", 5001);
    private Socket hostClientSocket;

    private boolean startSocket(int port) {
        hostClientSocket = bHostClient.connectSocket();
        log.info("connected to {}", hostClientSocket.getInetAddress().getHostAddress());

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Thread t = new MainThreadHandler(serverSocket);
            t.start();
            log.info("Started blocking Agent Server at port {}", port);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    class MainThreadHandler extends Thread {
        private ServerSocket serverSocket;

        public MainThreadHandler(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }
        @Override
        public void run() {
            while (true) {
                Socket s = null;
                try {
                    s = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Thread t = new BAgentServerHandler(s, hostClientSocket);
                t.start();
            }
        }
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
