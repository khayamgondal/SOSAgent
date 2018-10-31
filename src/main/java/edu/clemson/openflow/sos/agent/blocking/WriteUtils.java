package edu.clemson.openflow.sos.agent.blocking;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class WriteUtils {

    public static BufferedOutputStream hdos;

    public synchronized static void write(byte[] data, int available) throws IOException {
        hdos.write(data, 0, available);
        hdos.flush();
        System.out.println(available);
    }
}
