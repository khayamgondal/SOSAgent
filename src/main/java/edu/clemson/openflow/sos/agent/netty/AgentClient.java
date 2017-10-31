package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.agent.HostStatusInitiater;
import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.rest.RequestParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentClient implements HostStatusListener{
    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);

    private static final int AGENT_DATA_PORT = 9878;
   // private String agentServerIP;

    private Channel remoteChannel;
    private Channel myChannel;

    public AgentClient() {

    }
    public AgentClient (String agentServerIP, Channel remoteChannel) {
       // this.agentServerIP = agentServerIP;
        this.remoteChannel = remoteChannel;
    }

    public Channel getRemoteChannel() {
        return myChannel;
    }

    public void start(String agentServerIP) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new AgentClientChannelInitializer(remoteChannel));

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
    public void hostConnected(RequestParser request) {
        log.debug("new client connection from host {} port {}", request.getClientAgentIP(), request.getClientPort());
        start(request.getServerAgentIP());

    }

    @Override
    public void packetArrived(String hostIP, int hostPort, Object msg) {
        log.debug("Received new packet from host {} port {}", hostIP, hostPort);
        if (myChannel == null) log.error("Current context is null, wont be forwarding packet to other agent");
        else myChannel.writeAndFlush(msg);
    }

    @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }
}
