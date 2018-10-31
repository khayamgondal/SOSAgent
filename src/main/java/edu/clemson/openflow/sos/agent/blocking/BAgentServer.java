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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BAgentServer implements ISocketServer {
    private static final Logger log = LoggerFactory.getLogger(BAgentServer.class);
    private BHostClient bHostClient = new BHostClient("10.0.0.211", 5001);
    private Socket hostClientSocket;

    private boolean startSocket(int port) {
        hostClientSocket = bHostClient.connectSocket();
        log.info("connected to {} server", hostClientSocket.getInetAddress().getHostAddress());

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
        private int chNo = 0;
        public MainThreadHandler(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }
        @Override
        public void run() {
            Timer time = new Timer(); // Instantiate Timer Object
            ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
            time.schedule(st, 10000, 10000); // Create Repetitively task for every 10 secs

            while (true) {
                Socket s = null;
                try {
                    s = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Thread t = new BAgentServerHandler(s, hostClientSocket, chNo);
                t.start();
                chNo++;
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

    public class ScheduledTask extends TimerTask {

        long lastTime = System.currentTimeMillis(); // to display current time

        // Add your task here
        public void run() {
            long current = System.currentTimeMillis();

            System.out.println(WriteUtils.getTotalBytes() / (current - lastTime)) ;
            log.info("Received {} Gbps and Sent {} Gbps",
                    (WriteUtils.getTotalBytes() / (current - lastTime)) * 8 / 1024 / 1024,
                    (WriteUtils.getTotalSentBytes() / (current - lastTime)) * 8 / 1024 / 1024);

            WriteUtils.setTotalBytes(0);
            WriteUtils.setTotalSentBytes(0);
            lastTime = current;

            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            log.info("Total threads {}", threadSet.size());

        }
    }

}
