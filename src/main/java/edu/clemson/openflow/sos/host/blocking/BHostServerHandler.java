package edu.clemson.openflow.sos.host.blocking;

import edu.clemson.openflow.sos.agent.RRSendingStrategy;
import edu.clemson.openflow.sos.agent.SendingStrategy;
import edu.clemson.openflow.sos.agent.blocking.BAgentClient;
import edu.clemson.openflow.sos.agent.blocking.WriteUtils;
import edu.clemson.openflow.sos.buf.SeqGen;
import edu.clemson.openflow.sos.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BHostServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BHostServerHandler.class);
    private  DataInputStream din;

   // private BufferedInputStream dis = null;
    private DataOutputStream dos = null;
    private Socket s = null;
    private byte[] arrayToReadIn = new byte[80 * 1000 * 1000];

    SeqGen seqGen = new SeqGen();
    private BAgentClient bAgentClient;
    private List<Socket> socketList;
    private List<RemoteWrite> remoteWrites = new ArrayList<>();

    private int parllConns = Integer.parseInt(Utils.configFile.getProperty("test_conns").replaceAll("[\\D]", ""));; // also start this number of threads to write to remote agent


    private SendingStrategy sendingStrategy = new RRSendingStrategy(parllConns);
    ;

    public BHostServerHandler(Socket s) {
        try {
            this.s = s;
            InetAddress remoteSocketAddress = s.getInetAddress();
            InetAddress localSocketAddress = s.getLocalAddress();


        //    dis = new BufferedInputStream(s.getInputStream());

            din = new DataInputStream(s.getInputStream());

            dos = new DataOutputStream(s.getOutputStream());

            bAgentClient = new BAgentClient("10.0.0.12", 9878, parllConns);
            socketList = bAgentClient.connectSocks();
            for (int i = 0; i < parllConns; i++) {
                RemoteWrite remoteWrite = new RemoteWrite(socketList.get(i));
                remoteWrites.add(remoteWrite);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("WIll start {} conns", parllConns);
    }

    @Override
    public void run() {
        try {
            for (int i=0 ; i < remoteWrites.size() ; i ++) {
                startThread(i);
            }
            while (true) {
                int avail = din.available();
                if (avail > 0) {
                   // log.info("{}", dis.available());
                    WriteUtils.putSeq(arrayToReadIn);
                    din.read(arrayToReadIn, 3, din.available() + 4);

                    System.out.println(arrayToReadIn[0] + arrayToReadIn[1] + arrayToReadIn[2] + arrayToReadIn[3]);

                    //   seqGen.incomingPacket(b);
                  //  Socket curSock = socketList.get(sendingStrategy.channelToSendOn());

                    int chToSendOn = sendingStrategy.channelToSendOn();
                  //  log.info("Sending on channel {}", chToSendOn);
                    remoteWrites.get(chToSendOn).setData(arrayToReadIn, din.available());
                    WriteUtils.addSentBytes(din.available());
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
        private int available;

        public RemoteWrite(Socket socket) {
            this.socket = socket;
        }

        public synchronized void setData(byte[] data, int available) {
            this.data = data;
            this.available = available;
        }

        @Override
        public void run() {
                try {
                    while (true) {
                        if (data != null && available !=0 )  write(data);
                        if (s.isClosed()) {
                            log.info("client done sending closing agent socket");
                            socket.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        private void write(byte[] data) throws IOException {
            // log.info("Sending {}", available);
            // System.out.println(available);
            socket.getOutputStream().write(data, 0, available);
            socket.getOutputStream().flush();
        }
    }
}
