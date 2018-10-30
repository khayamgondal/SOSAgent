package edu.clemson.openflow.sos.host.blocking;

import edu.clemson.openflow.sos.agent.RRSendingStrategy;
import edu.clemson.openflow.sos.agent.SendingStrategy;
import edu.clemson.openflow.sos.agent.blocking.BAgentClient;
import edu.clemson.openflow.sos.buf.SeqGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BHostServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BHostServerHandler.class);

    private BufferedInputStream dis = null;
    private BufferedOutputStream dos = null;
    private Socket s = null;
    private byte[] arrayToReadIn = new byte[1659176 + 1000];

    SeqGen seqGen = new SeqGen();
    private BAgentClient bAgentClient;
    private List<Socket> socketList;
    private List<RemoteWrite> remoteWrites = new ArrayList<>();

    private int parllConns = 16; // also start this number of threads to write to remote agent


    private SendingStrategy sendingStrategy = new RRSendingStrategy(parllConns);
    ;

    public BHostServerHandler(Socket s) {
        try {
            this.s = s;
            InetAddress remoteSocketAddress = s.getInetAddress();
            InetAddress localSocketAddress = s.getLocalAddress();

            dis = new BufferedInputStream(s.getInputStream());
            dos = new BufferedOutputStream(s.getOutputStream());

            bAgentClient = new BAgentClient("10.0.0.12", 9878, parllConns);
            socketList = bAgentClient.connectSocks();
            for (int i = 0; i < parllConns; i++) {
                RemoteWrite remoteWrite = new RemoteWrite(socketList.get(i));
                remoteWrites.add(remoteWrite);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            for (int i=0 ; i < remoteWrites.size() ; i ++) {
                startThread(i);
            }
            while (true) {
                int avail = dis.available();
                if (avail > 0) {
                   // log.info("{}", dis.available());
                    dis.read(arrayToReadIn);
                    //   seqGen.incomingPacket(b);
                  //  Socket curSock = socketList.get(sendingStrategy.channelToSendOn());

                    int chToSendOn = sendingStrategy.channelToSendOn();
                  //  log.info("Sending on channel {}", chToSendOn);
                    remoteWrites.get(chToSendOn).setData(arrayToReadIn);
                  //  startThread(chToSendOn);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void startThread(int chToSendOn) {
        if (! remoteWrites.get(chToSendOn).isAlive()) remoteWrites.get(chToSendOn).start();

    }

    public class RemoteWrite extends Thread {
        private Socket socket;
        private boolean isDataAvaiable;
        private byte[] data;

        public RemoteWrite(Socket socket) {
            this.socket = socket;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
                try {
                    while (true) {
                        if (data != null) write(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        private void write(byte[] data) throws IOException {
            log.info("Sending {}", data.length);
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        }
    }
}
