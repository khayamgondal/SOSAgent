package edu.clemson.openflow.sos.agent.blocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BAgentServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BAgentServerHandler.class);
    private Socket socket;

    public BAgentServerHandler(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        log.info("connected to {}", socket.getInetAddress().getHostAddress());
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

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
