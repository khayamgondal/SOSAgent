package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.agent.HostStatusInitiater;
import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.rest.RequestParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentClient extends ChannelInboundHandlerAdapter implements HostStatusListener{
    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);

    private static final int AGENT_DATA_PORT = 9878;
    private Channel myChannel;
    private HostStatusInitiater callBackHostStatusInitiater;

    public AgentClient() {

    }

    public void start(String agentServerIP) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast("bytesDecoder",
                                    new ByteArrayDecoder());
                            channel.pipeline().addLast("agentClient", new AgentClient());
                        }
                    });

            this.myChannel = bootstrap.connect(agentServerIP, AGENT_DATA_PORT).sync().channel();
            log.info("Connected to Agent-Server {} on Port {}", agentServerIP, AGENT_DATA_PORT);


        } catch (Exception e) {
            log.error("Error connecting to Agent-Server {} on Port{}", agentServerIP, AGENT_DATA_PORT);
            e.printStackTrace();
        } finally {
            //group.shutdownGracefully();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Reading from remote agent");
        callBackHostStatusInitiater.packetArrived(msg); // send back to host side
    }

    @Override
    public void hostConnected(RequestParser request, HostStatusInitiater callBackhostStatusInitiater) {
        this.callBackHostStatusInitiater = callBackhostStatusInitiater;
        log.debug("new client connection from host {} port {}", request.getClientAgentIP(), request.getClientPort());

    }

    @Override
    public void packetArrived(Object msg) { //write this packet
        log.debug("Received new packet from host");
        if (myChannel == null) log.error("Current channel is null, wont be forwarding packet to other agent");
        else {
            log.debug("Forwarding to {}", myChannel.remoteAddress().toString());
            myChannel.writeAndFlush(msg);
        }
    }

    @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }
}
