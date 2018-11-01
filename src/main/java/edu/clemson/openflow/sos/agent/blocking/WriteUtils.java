package edu.clemson.openflow.sos.agent.blocking;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class WriteUtils {

    public static DataOutputStream hdos;
    private static long totalBytes;
    private static long totalSentBytes;

    private static int seq;

    public synchronized static void write(byte[] data, int available) throws IOException {

        hdos.write(data, 0, available);
        hdos.flush();
       // System.out.println(available);
    }

    public static void setTotalBytes(long totalBytes) {
        WriteUtils.totalBytes = totalBytes;
    }

    public static void setTotalSentBytes(long totalSentBytes) {
        WriteUtils.totalSentBytes = totalSentBytes;
    }

    public static long getTotalSentBytes() {
        return totalSentBytes;
    }

    public static long getTotalBytes() {
        return totalBytes;
    }

    public synchronized static void addBytes(long totalBytes) {
        WriteUtils.totalBytes += totalBytes;
    }
    public synchronized static void addSentBytes(long totalBytes) {
        WriteUtils.totalSentBytes += totalBytes;
    }

    public synchronized static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public synchronized static byte[] putSeq(byte[] result) {

        if (seq == 500) seq = 0;

        result[0] = (byte) (seq >> 24);
        result[1] = (byte) (seq >> 16);
        result[2] = (byte) (seq >> 8);
        result[3] = (byte) (seq /*>> 0*/);



        seq++;

        return result;
    }
}
