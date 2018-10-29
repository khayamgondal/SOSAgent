package edu.clemson.openflow.sos.host.blocking;

import edu.clemson.openflow.sos.agent.RRSendingStrategy;
import edu.clemson.openflow.sos.agent.SendingStrategy;
import edu.clemson.openflow.sos.agent.blocking.BAgentClient;
import edu.clemson.openflow.sos.buf.SeqGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class BHostServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BHostServerHandler.class);

    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private Socket s = null;
    byte[] b  = new byte[269608+10];
    SeqGen seqGen = new SeqGen();
    private BAgentClient bAgentClient;
    private List<Socket> socketList;
    private SendingStrategy sendingStrategy = new RRSendingStrategy(16);;

    public BHostServerHandler(Socket s) {
        try {
            this.s = s;
            InetAddress remoteSocketAddress = s.getInetAddress();
            InetAddress localSocketAddress = s.getLocalAddress();
             dis = new DataInputStream(s.getInputStream());
             dos = new DataOutputStream(s.getOutputStream());
             bAgentClient = new BAgentClient("10.0.0.12", 9878, 15);
             socketList = bAgentClient.connectSocks();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                int avail = dis.available();
                if (avail > 0) {
                    log.info("{}", dis.available());
                    dis.read(b, 0, dis.available());
                    seqGen.incomingPacket(b);
                    Socket curSock = socketList.get(sendingStrategy.channelToSendOn());
                    DataOutputStream dos = new DataOutputStream(curSock.getOutputStream());
                    dos.write(b);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
