package edu.clemson.openflow.sos.host;

import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.agent.AgentClient;
import edu.clemson.openflow.sos.buf.SeqGen;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RequestMapper;
import edu.clemson.openflow.sos.utils.EventListenersLists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 * Listens for connections from client on port 9877
 */

public class HostServer extends ChannelInboundHandlerAdapter implements ISocketServer, RequestListener {
    private static final Logger log = LoggerFactory.getLogger(HostServer.class);
    private static final int DATA_PORT = 9877;
    private RequestMapper request;
    //private Channel myChannel;
    //PacketBuffer packetBuffer;
    private SeqGen seqGen;
    private AgentClient agentClient;
    private List<RequestMapper> incomingRequests = new ArrayList<>();
    private NioEventLoopGroup group;
    private HostStatusInitiator hostStatusInitiator;


    public HostServer() {
        EventListenersLists.requestListeners.add(this); //we register for incoming requests
    }

    public class HostServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            log.info("New host-side connection from {} at Port {}",
                    socketAddress.getHostName(),
                    socketAddress.getPort());
            //TODO: If remotely connecting client is in your /etc/hosts than socketAddress.getHostName() will return that hostname instead of its IP address and following method call will return null
            request = getClientRequest(socketAddress.getHostName(), socketAddress.getPort()); // go through the list and find related request
            if (request == null) {
                log.error("No controller request found for this associated port ...all incoming packets will be dropped ");
                return;
            }
         //   myChannel = ctx.channel();

            if (request != null) {
                seqGen = new SeqGen();
                agentClient = new AgentClient(request);
                agentClient.setChannel(ctx.channel());

                hostStatusInitiator = new HostStatusInitiator();
                hostStatusInitiator.addListener(agentClient);

            }
            else log.error("Couldn't find the request {} in request pool. Not notifying agent",
                    request.toString());

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.info("Client is done sending");
            hostStatusInitiator.hostStatusChanged(HostStatusListener.HostStatus.DONE); // notify Agent Client that host is done sending
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {

            if (request != null && seqGen != null) {
                    agentClient.incomingPacket(seqGen.incomingPacket((byte[]) msg)); // forward packet to agentClient
                 }
            else log.error("Couldn't find the request. Not forwarding packet");
          //  ReferenceCountUtil.release(msg);
        }
    }

    private boolean startSocket(int port) {
        group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer() {
                                      @Override
                                      protected void initChannel(Channel channel) throws Exception {
                                          channel.pipeline().addLast("bytesDecoder",
                                                  new ByteArrayDecoder());
                                          channel.pipeline().addLast("hostHandler", new HostServerHandler());
                                          channel.pipeline().addLast("bytesEncoder", new ByteArrayEncoder());
                                      }
                                  }
                    );
            ChannelFuture f = b.bind().sync();
            //myChannel = f.channel();
            log.info("Started host-side socket server at Port {}", port);
            return true;
        } catch (InterruptedException e) {
            log.error("Error starting host-side socket");
            e.printStackTrace();
            return false;
        }
    }
    private RequestMapper getClientRequest(String remoteIP, int remotePort) {
        // if its a mock request we dont need to match the port.. just match on IP
        for (RequestMapper incomingRequest : incomingRequests) {
            if (incomingRequest.getRequest().getClientIP().equals(remoteIP) &&
                    incomingRequest.getRequest().getClientPort() == remotePort) return incomingRequest;
        }
        return null;
    }

    @Override
    public boolean start() {
        return startSocket(DATA_PORT);
    }

    @Override
    public boolean stop() {
        group.shutdownGracefully();
        log.info("Host Server shutting down");
        return true;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void newIncomingRequest(RequestMapper request) {
        incomingRequests.add(request);

    }
}
