package edu.clemson.openflow.sos.buf;

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
    private byte[] seqNo2 = new byte[4];
    private static final int MAX_SEQ = 30000; //Integer.MAX_VALUE;


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

    public ByteBuf incomingPacket(ByteBuf packet) {
        if (seqNo == MAX_SEQ) seqNo = 0;
        //ByteBuffer toSend = ByteBuffer.allocate(4 + packet.length);

       // ByteBuf toSend.putInt(seqNo)
          //      .put(packet);
        //log.debug("prepended seq no. {}.. ", toSend.getInt(0));
        //seqNo++;
       // return toSend.array();
        return Unpooled.wrappedBuffer(seqNo, packet);
    }
   // ByteBuffer b = ByteBuffer.allocate(4);

    public ByteBuf incomingPacket1(byte[] packet) {

        if (seqNo == MAX_SEQ) seqNo = 0;
        ByteBuffer b = ByteBuffer.allocate(4); // also release b ? why not create large buffer than mark it unmark it and sent the ref ??/
        b.putInt(0, seqNo);
        seqNo++;
       // log.info("before size {}", packet.length);
        return Unpooled.wrappedBuffer(b.array(), packet);
    }
}
