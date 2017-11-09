package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.agent.HostStatusInitiator;
import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentClient implements HostStatusListener {
    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);

    private static final int AGENT_DATA_PORT = 9878;
    //private Channel myChannel;
    private HostStatusInitiator hostStatusInitiator;
    private AgentClientHandler agentClientHandler;

    public class AgentClientHandler extends ChannelInboundHandlerAdapter {


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("Reading from remote agent");
            hostStatusInitiator.packetArrived(msg); // send back to host side
        }

    }

    public AgentClient() {
        agentClientHandler = new AgentClientHandler();

    }

    public Channel bootStrap(EventLoopGroup group, String agentServerIP) {
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast("bytesDecoder", new ByteArrayDecoder())
                                    .addLast("agentClient", new AgentClientHandler())
                                    .addLast("bytesEncoder", new ByteArrayEncoder());
                        }
                    });
            ;

            Channel myChannel = bootstrap.connect(agentServerIP, AGENT_DATA_PORT).sync().channel();
            //if (myChannel == null) log.debug("in start it is nul");
            log.debug("Connected to Agent-Server {} on Port {}", agentServerIP, AGENT_DATA_PORT);
            return myChannel;

        } catch (Exception e) {
            log.error("Error connecting to Agent-Server {} on Port{}", agentServerIP, AGENT_DATA_PORT);
            e.printStackTrace();
            return null;
        } finally {
            //group.shutdownGracefully();
        }
    }

    public NioEventLoopGroup createEventLoopGroup() {
        return new NioEventLoopGroup();
    }
   // public void start(String agentServerIP) {
   //     EventLoopGroup eventLoopGroup = createEventLoopGroup();
        //bootStrap(eventLoopGroup, agentServerIP);
   // }

   // public Channel bootStrap(EventLoopGroup eventLoopGroup, String remoteIP) {
   //     return bootStrap(eventLoopGroup, remoteIP);
   // }

   @Override
    public void hostConnected(ControllerRequestMapper request, Object callBackObject) {
  /*       hostStatusInitiator = new HostStatusInitiator();
        hostStatusInitiator.addListener((HostServer) callBackObject);
        log.debug("new client connection from host {} port {}", request.getClientAgentIP(), request.getClientPort());
        start(request.getServerAgentIP());
  */  }

    @Override
    public void packetArrived(Object msg) { //write this packet
   /*     log.debug("Received new packet from host");
        if (myChannel == null) log.error("Current channel is null, wont be forwarding packet to other agent");
        else {
            log.debug("Forwarding to {}", myChannel.remoteAddress().toString());
            //   byte[] d = (byte[]) msg;
            //   String s = new String(d);
            //   log.debug("KKK {}", s);
            myChannel.writeAndFlush(msg);
        }*/
    }

    @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }
}
