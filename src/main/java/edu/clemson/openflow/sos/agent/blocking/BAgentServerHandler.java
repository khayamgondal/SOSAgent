package edu.clemson.openflow.sos.agent.blocking;

import edu.clemson.openflow.sos.host.blocking.BHostClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;

public class BAgentServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BAgentServerHandler.class);
    private Socket socket;

    private BHostClient bHostClient = new BHostClient("10.0.0.211", 5001);
    private Socket hostClientSocket;

    public BAgentServerHandler(Socket s) {
        socket = s;
        hostClientSocket = bHostClient.connectSocket();
    }

    @Override
    public void run() {
        log.info("connected to {}", socket.getInetAddress().getHostAddress());
        DataInputStream dis = null;
        DataOutputStream dos = null;
        BufferedReader hdis = null;
        DataOutputStream hdos = null;

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            hdis = new BufferedReader( new InputStreamReader(hostClientSocket.getInputStream()));
            hdos = new DataOutputStream(hostClientSocket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                int avail = dis.available();
                if (avail > 0) {
                    log.info("{}", dis.available());
                    dis.read(b, 0, dis.available());

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
