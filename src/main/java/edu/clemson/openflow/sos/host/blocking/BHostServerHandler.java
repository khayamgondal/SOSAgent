package edu.clemson.openflow.sos.host.blocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class BHostServerHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(BHostServerHandler.class);

    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private Socket s = null;
    byte[] b  = new byte[65000];
    public BHostServerHandler(Socket s) {
        try {
            this.s = s;
            InetAddress remoteSocketAddress = s.getInetAddress();
            InetAddress localSocketAddress = s.getLocalAddress();
             dis = new DataInputStream(s.getInputStream());
             dos = new DataOutputStream(s.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                //dis.read(b);
                int avail = dis.available();
                if (avail > 0)
                 log.info("{}", dis.available());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
