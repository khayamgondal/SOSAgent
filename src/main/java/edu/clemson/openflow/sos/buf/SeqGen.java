package edu.clemson.openflow.sos.buf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This class will append the sequence no. and will forward formed packet to AgentClient
 */
public class SeqGen {
    private static final Logger log = LoggerFactory.getLogger(SeqGen.class);

    private int seqNo = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;


    //TODO: check performance when instead of byteBuffer bytebuf is used
    public byte[] incomingPacket(byte[] packet) {
        if (seqNo == MAX_SEQ) seqNo = 0;
        ByteBuffer toSend = ByteBuffer.allocate(4 + packet.length);
        toSend.putInt(seqNo)
                .put(packet);
        log.debug("prepended seq no. {}.. ", toSend.getInt(0));
        seqNo++;
        return toSend.array();
    }

}
