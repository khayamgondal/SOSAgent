package edu.clemson.openflow.sos.host;

import edu.clemson.openflow.sos.agent.HostPacketInitiator;
import edu.clemson.openflow.sos.buf.SeqGen;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.stats.StatCollector;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class HostClient implements HostStatusListener {
    private static final Logger log = LoggerFactory.getLogger(HostClient.class);

    private Channel myChannel;
    private SeqGen seqGen;
    private EventLoopGroup group;
    private boolean shutdDowned;
    private RequestTemplateWrapper request;

    private HostPacketInitiator hostPacketInitiator;

    public HostClient() {
        seqGen = new SeqGen();
    }

    public HostClient(RequestTemplateWrapper request) {
        seqGen = new SeqGen();
        this.request = request;
    }


    public void setHostPacketListenerInitiator(HostPacketInitiator initiator) {
        hostPacketInitiator = initiator;
    }

    @Override
    public synchronized void HostStatusChanged(HostStatus hostStatus) {

        if (!shutdDowned && hostStatus == HostStatus.DONE) {
            shutdDowned = true;
            log.info("Client {}:{} to Server {}:{} is done",
                    request.getRequest().getClientIP(),
                    request.getRequest().getClientPort(),
                    request.getRequest().getServerIP(),
                    request.getRequest().getServerPort());
            group.shutdownGracefully();
            StatCollector.getStatCollector().hostRemoved();

        }
    }


    public void start(String hostServerIP, int hostServerPort) {
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)

                    //          High watermark:
                    //If the number of bytes queued in the write buffer exceeds writeBufferHighWaterMark value,
                    // Channel.isWritable() will start to return false.
                    //          Low watermark:
                    //Once the number of bytes queued in the write buffer exceeded the high water mark and then
                    // dropped down below this value, Channel.isWritable() will return true again.
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 2048 * 1024, 8 * 2048 * 1024))
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast("bytesDecoder", new ByteArrayDecoder())
                                    .addLast("hostClient", new HostClientHandler())
                                    .addLast("bytesEncoder", new ByteArrayEncoder())
                            ;
                        }
                    });

            myChannel = bootstrap.connect(hostServerIP, hostServerPort).sync().channel();
            InetSocketAddress socketAddress = (InetSocketAddress) myChannel.localAddress();

            log.info("Client {}:{} connected to Server {}:{}",
                    request.getRequest().getClientIP(),
                    request.getRequest().getClientPort(),
                    hostServerIP,
                    hostServerPort);
        } catch (Exception e) {
            log.error("Error connecting Client {}:{} to Server {}:{}",
                    request.getRequest().getClientIP(),
                    request.getRequest().getClientPort(),
                    hostServerIP,
                    hostServerPort);
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


    class HostClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            StatCollector.getStatCollector().hostAdded();

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (hostPacketInitiator != null) {
                ByteBuf seqed = seqGen.incomingPacket((byte[]) msg);
                hostPacketInitiator.hostPacket(seqed);
            }
            // initiator.hostPacket(seqGen.incomingPacket((byte[]) msg)); //notify the listener
        }

    }

}

