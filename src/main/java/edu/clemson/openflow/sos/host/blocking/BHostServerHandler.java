package edu.clemson.openflow.sos.host.blocking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class BHostServerHandler extends Thread {
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private Socket s = null;
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
        dis.r
        super.run();
    }
}
