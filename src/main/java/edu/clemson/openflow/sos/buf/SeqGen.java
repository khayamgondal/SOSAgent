package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
    private static int MAX_SEQ = 100; //Integer.MAX_VALUE;

    public SeqGen() {
        if (Utils.configFile != null)
            MAX_SEQ = Integer.parseInt(Utils.configFile.getProperty("buffer_size").replaceAll("[\\D]", ""));
        log.debug("Max sequence no is {}", MAX_SEQ);
    }

    public ByteBuf incomingPacket(byte[] packet) {

        if (seqNo == MAX_SEQ) seqNo = 0;
        ByteBuffer b = ByteBuffer.allocate(4); // also release b ? why not create large buffer than mark it unmark it and sent the ref ??/
        b.putInt(0, seqNo);
        seqNo++;
        return Unpooled.wrappedBuffer(b.array(), packet);
    }
    public byte incomingPacket(byte[] packet) {

        if (seqNo == MAX_SEQ) seqNo = 0;
        ByteBuffer b = ByteBuffer.allocate(4); // also release b ? why not create large buffer than mark it unmark it and sent the ref ??/
        b.putInt(0, seqNo);
        seqNo++;
        return ByteBuffer.wrap(b, packet);
    }
}
