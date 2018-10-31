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
    private int chNo;

    private Socket hostClientSocket;
    private byte[] arrayToReadIn = new byte[80 * 1000 * 1000];

    BufferedInputStream hdis = null;
    BufferedOutputStream hdos = null;


    public BAgentServerHandler(Socket s, Socket hostClientSocket, int chNo) {
        socket = s;
        this.hostClientSocket = hostClientSocket;
        this.chNo = chNo;
        try {
            hdis = new BufferedInputStream(socket.getInputStream());
            hdos = new BufferedOutputStream(hostClientSocket.getOutputStream());
            WriteUtils.hdos = hdos;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        log.info("connected to agent {}", socket.getInetAddress().getHostAddress());
        try {
            while (true) {
                if (hdis.available() > 0) {
                   log.info("received {} on Ch {}", hdis.available(), chNo);
                   hdis.read(arrayToReadIn);
                   WriteUtils.write(arrayToReadIn, hdis.available());

                }
                if (socket.isClosed() && !hostClientSocket.isClosed()) {
                    hostClientSocket.close();
                    log.info("Client agent disconnected");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
