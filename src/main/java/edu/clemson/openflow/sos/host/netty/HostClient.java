package edu.clemson.openflow.sos.host.netty;

import edu.clemson.openflow.sos.agent.DataPipelineInitiator;
import edu.clemson.openflow.sos.agent.DataPipelineListener;
import edu.clemson.openflow.sos.agent.netty.AgentServer;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostClient implements DataPipelineListener {
    private static final Logger log = LoggerFactory.getLogger(HostClient.class);
    private DataPipelineInitiator dataPipelineInitiator;
    private Channel myChannel;


    public HostClient() {
    }

    class HostClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("Reading from host");
            dataPipelineInitiator.packetArrived(msg); // send back to host side
        }

    }

    public void start(String hostServerIP, int hostServerPort) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast("bytesDecoder", new ByteArrayDecoder())
                                    .addLast("hostClient", new HostClientHandler())
                                    .addLast("bytesEncoder", new ByteArrayEncoder());
                        }
                    });
            myChannel = bootstrap.connect(hostServerIP, hostServerPort).sync().channel();
            log.info("Connected to Host-Server {} on Port {}", hostServerIP, hostServerPort);


        } catch (Exception e) {
            log.error("Error connecting to Host-Server {} on Port{}", hostServerIP, hostServerPort);
            e.printStackTrace();
        } finally {
            //group.shutdownGracefully();
        }
    }


    @Override
    public void hostConnected(ControllerRequestMapper request, Object callBackObject) {
        start(request.getServerIP(), request.getServerPort());
        dataPipelineInitiator = new DataPipelineInitiator();
        dataPipelineInitiator.addListener((AgentServer) callBackObject);
        log.debug("new connection from agent {} port {}", request.getClientAgentIP(), request.getClientPort());

    }

    @Override
    public void packetArrived(Object msg) { //write this packet
        log.debug("Received new packet from remote agent");
        if (myChannel == null) log.error("Current channel is null, wont be forwarding packet to other agent");
        else myChannel.writeAndFlush(msg);
    }

    @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }
}

