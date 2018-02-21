package edu.clemson.openflow.sos.agent;

/**
 * @author khayam anjam kanjam@g.clemson.edu
 * This class receives data form AgentClient and writes into Buffer
 */

import edu.clemson.openflow.sos.buf.*;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.rest.RequestMapper;
import edu.clemson.openflow.sos.utils.EventListenersLists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class AgentServer implements ISocketServer, RequestListener {
    private static final int AGENT_DATA_PORT = 9878;
    private static final Logger log = LoggerFactory.getLogger(AgentServer.class);

    private RequestMapper request;
    private BufferManager bufferManager;
    private AgentToHostManager hostManager;

    private List<RequestMapper> incomingRequests;
    private NioEventLoopGroup group;

    public AgentServer() {
        incomingRequests = new ArrayList<>();
        EventListenersLists.requestListeners.add(this);
        bufferManager = new BufferManager(); //setup buffer manager.
        hostManager = new AgentToHostManager();
    }

    public class AgentServerHandler extends ChannelInboundHandlerAdapter {
        private Buffer myBuffer;
        private AgentToHost myHost;
        private String remoteAgentIP;
        private int remoteAgentPort;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            log.debug("New agent-side connection from agent {} at Port {}",
                    socketAddress.getHostName(),
                    socketAddress.getPort());

            remoteAgentIP = socketAddress.getHostName();
            remoteAgentPort = socketAddress.getPort();

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (request == null) { //this could be moved to active. need to check performance
                request = getMyRequestByClientAgentPort(remoteAgentIP, remoteAgentPort); // go through the list and find related request
                myHost = hostManager.addAgentToHost(request);
                myHost.addChannel(ctx.channel());
                myBuffer = bufferManager.addBuffer(request, myHost); //passing callback listener so when sorted packets are avaiable it can notify the agent2host

            }
            if (request == null) {
                ReferenceCountUtil.release(msg);
                log.error("No request found .. releasing received packets");
                return;
            }
            //     ByteBuf bytes = Unpooled.wrappedBuffer((byte[]) msg); //PERFORMANCE
            ByteBuf bytes = (ByteBuf) msg;
            log.debug("Got packet with seq {} & size {}", bytes.getInt(0), bytes.capacity());
            if (myBuffer == null) log.error("BUFFER NULL for {} ... wont be writing packets", remoteAgentPort);
            else myBuffer.incomingPacket(bytes);
            ReferenceCountUtil.release(bytes);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.info("Channel is inactive");
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
                                          channel.pipeline()
                                                  .addLast("lengthdecorder",
                                                          new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                                  // .addLast("bytesDecoder", new ByteArrayDecoder())
                                                  .addLast(new AgentServerHandler())
                                                  .addLast("4blength", new LengthFieldPrepender(4))
                                                  .addLast("bytesEncoder", new ByteArrayEncoder())
                                          ;
                                      }
                                  }
                    );

            ChannelFuture f = b.bind().sync();
            log.info("Started agent-side server at Port {}", port);
            return true;
            // Need to do socket closing handling. close all the remaining open sockets
            //System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            //f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error starting agent-side server");
            e.printStackTrace();
            return false;
        } finally {
            //group.shutdownGracefully().sync();
        }
    }

    private RequestMapper getMyRequestByClientAgentPort(String remoteIP, int remotePort) {
        for (RequestMapper incomingRequest : incomingRequests) {
            if (incomingRequest.getRequest().getClientAgentIP().equals(remoteIP)) {
                for (int port : incomingRequest.getPorts()
                        ) {
                    if (port == remotePort) return incomingRequest;
                }
            }
        }
        return null;
    }

    @Override
    public boolean start() {
        return startSocket(AGENT_DATA_PORT);
    }

    @Override
    public boolean stop() {
        group.shutdownGracefully();
        log.info("Shutting down AgentServer");
        return true;
    }


    @Override
    public void newIncomingRequest(RequestMapper request) {
        incomingRequests.add(request);
        // packetBuffers.add(packetBuffer);

        log.debug("Received ports info from client agent {}", request.toString());
    }
}
