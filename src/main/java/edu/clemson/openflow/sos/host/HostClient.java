package edu.clemson.openflow.sos.host;

import edu.clemson.openflow.sos.agent.HostPacketInitiator;
import edu.clemson.openflow.sos.agent.AgentToHost;
import edu.clemson.openflow.sos.buf.SeqGen;
import edu.clemson.openflow.sos.stats.StatCollector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostClient implements HostStatusListener{
    private static final Logger log = LoggerFactory.getLogger(HostClient.class);
    private Channel myChannel;
    private SeqGen seqGen;
    private EventLoopGroup group;

    private HostPacketInitiator initiator;
    public HostClient(){
        seqGen = new SeqGen();
        initiator = new HostPacketInitiator();
    }

    public void setListener(Object listener) {
        initiator.addListener((AgentToHost)listener);
    }

    @Override
    public void HostStatusChanged(HostStatus hostStatus) {
        if (!group.isShutdown() && hostStatus == HostStatus.DONE) {
            log.debug("Client is done sending ... closing socket");
            group.shutdownGracefully();
            StatCollector.getStatCollector().hostRemoved();
        }
    }

    class HostClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            StatCollector.getStatCollector().hostAdded();

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("Reading from host");
          //  byte[] packet = seqGen.incomingPacket((byte[]) msg);
            initiator.hostPacket(seqGen.incomingPacket1((byte[]) msg)); //notify the listener
        }

    }

    public void start(String hostServerIP, int hostServerPort) {
        group = new NioEventLoopGroup();
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

    public Channel getMyChannel() {
        return myChannel;
    }

}

