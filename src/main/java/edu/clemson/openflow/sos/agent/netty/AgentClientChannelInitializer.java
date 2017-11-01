package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.host.netty.HostServerChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class AgentClientChannelInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("bytesDecoder",
                new ByteArrayDecoder());
        socketChannel.pipeline().addLast("agentClient", new AgentClientChannelHandler());

        // Encoder
        //socketChannel.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
        socketChannel.pipeline().addLast("bytesEncoder", new ByteArrayEncoder());
    }
}
