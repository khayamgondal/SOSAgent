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

    private Socket hostClientSocket;
    private byte[] arrayToReadIn = new byte[1659176 + 1000];

    BufferedInputStream hdis = null;
    BufferedOutputStream hdos = null;


    public BAgentServerHandler(Socket s, Socket hostClientSocket) {
        socket = s;
        this.hostClientSocket = hostClientSocket;
        try {
            hdis = new BufferedInputStream(socket.getInputStream());
            hdos = new BufferedOutputStream(hostClientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        log.info("connected to {}", socket.getInetAddress().getHostAddress());


        try {
            while (true) {
                int avail = hdis.available();
                if (avail > 0) {
               //     log.info("{}", dis.available());
                  hdis.read(arrayToReadIn);
                  write(arrayToReadIn);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void write(byte[] data) throws IOException {
        hdos.write(data);
        hdos.flush();
    }
}
