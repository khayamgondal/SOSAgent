package edu.clemson.openflow.sos.host;

import edu.clemson.openflow.sos.agent.AgentToHost;
import edu.clemson.openflow.sos.agent.HostPacketInitiator;
import edu.clemson.openflow.sos.buf.SeqGen;
import edu.clemson.openflow.sos.stats.StatCollector;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class HostClient implements HostStatusListener {
    private static final Logger log = LoggerFactory.getLogger(HostClient.class);
    private Channel myChannel;
    private SeqGen seqGen;
    private EventLoopGroup group;

    private HostPacketInitiator hostPacketInitiator;

    public HostClient() {
        seqGen = new SeqGen();
        //initiator = new HostPacketInitiator();
    }

    public void setListener(Object listener) {
       // initiator.addListener((AgentToHost) listener);
    }

    public void setHostPacketListenerInitiator(HostPacketInitiator initiator) {
        hostPacketInitiator = initiator;
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
            //  byte[] packet = seqGen.incomingPacket((byte[]) msg);
            if (hostPacketInitiator != null) {
                ByteBuf seqed = seqGen.incomingPacket((byte[]) msg);
                hostPacketInitiator.hostPacket(seqed);
            }
           // initiator.hostPacket(seqGen.incomingPacket((byte[]) msg)); //notify the listener
        }

    }

    public void start(String hostServerIP, int hostServerPort) {
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    //  .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 256 * 1024)
                    //  .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 255 * 1024)

                    //          High watermark:
                    //If the number of bytes queued in the write buffer exceeds writeBufferHighWaterMark value,
                    // Channel.isWritable() will start to return false.
                    //          Low watermark:
                    //Once the number of bytes queued in the write buffer exceeded the high water mark and then
                    // dropped down below this value, Channel.isWritable() will return true again.
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(2048 * 1024, 2048 * 1024))
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    //    .addLast("bytesDecoder", new ByteArrayDecoder())
                                    .addLast("hostClient", new HostClientHandler())
                            //  .addLast("bytesEncoder", new ByteArrayEncoder())
                            ;
                        }
                    });

            myChannel = bootstrap.connect(hostServerIP, hostServerPort).sync().channel();
            InetSocketAddress socketAddress = (InetSocketAddress) myChannel.localAddress();

            log.info("Connected :{} to Host-Server {} on Port {}",socketAddress.getPort() ,hostServerIP, hostServerPort);

            //  ChannelFuture channelFuture = bootstrap.connect().sync();
            //  channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("Error connecting to Host-Server {} on Port{}", hostServerIP, hostServerPort);
            e.printStackTrace();
        } finally {
         /*   try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    public Channel getHostChannel() {
        return myChannel;
    }

}

