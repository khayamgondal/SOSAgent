package edu.clemson.openflow.sos.agent;

/**
 * @author khayam anjam kanjam@g.clemson.edu
 * This class receives data form AgentClient and writes into Buffer
 */

import edu.clemson.openflow.sos.buf.*;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.stats.StatCollector;
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

public class AgentServer implements ISocketServer {
    private static final int AGENT_DATA_PORT = 9878;
    private static final Logger log = LoggerFactory.getLogger(AgentServer.class);

    private BufferManager bufferManager;
    private AgentToHostManager hostManager;

    private List<RequestTemplateWrapper> incomingRequests;
    private NioEventLoopGroup group;

    public AgentServer() {
        incomingRequests = new ArrayList<>();
        bufferManager = new BufferManager(); //setup buffer manager.
        hostManager = new AgentToHostManager();
    }

    public class AgentServerHandler extends ChannelInboundHandlerAdapter implements RequestListener {

        private Buffer myBuffer;
        private AgentToHost myEndHost;
        private String remoteAgentIP;
        private int remoteAgentPort;
        private Channel myChannel;
        private float totalBytes;
        private long startTime;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            log.debug("New agent-side connection from agent {} at Port {}",
                    socketAddress.getHostName(),
                    socketAddress.getPort());

            remoteAgentIP = socketAddress.getHostName();
            remoteAgentPort = socketAddress.getPort();
            myChannel = ctx.channel();
            EventListenersLists.requestListeners.add(this);
            StatCollector.getStatCollector().connectionAdded();
            startTime = System.currentTimeMillis();

        }

    /*    @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
*/
        /*
                    Whenever AgentServer receives new port request from AgentClient. This method will be called and all the open channels
                    will be notified. So considering there is are previous open connections and AS receives new request it will also notify
                    those old channels but they have a null check on myEndHost which will prevent them from using new request.
                    However if two client try to connect at same time it can show undesired behaviour
                    TODO: Do something better
                 */
        @Override
        public void newIncomingRequest(RequestTemplateWrapper request) {

                if (myEndHost == null) {
                    log.debug("Setting up receive buffer for this connection. My end-host is {} {}", request.getRequest().getServerIP(), request.getRequest().getServerPort());
                    myEndHost = hostManager.addAgentToHost(request);
                    myEndHost.addChannel(myChannel);
                    myBuffer = bufferManager.addBuffer(request, myEndHost); //passing callback listener so when sorted packets are avaiable it can notify the agent2host
                }
            }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
      //      byte[] dd = new byte[10];
       //     ((ByteBuf) msg).getBytes(10, dd);
           // log.info(new String(dd));
        //    log.info("Rec seq {} size {} bytes {}",((ByteBuf) msg).getInt(0) , ((ByteBuf) msg).capacity(), dd);

        //    ByteBuf bytes = (ByteBuf) msg;
        //    log.debug("Got packet with seq {} & size {} from Agent-Client", bytes.getInt(0), bytes.capacity());
              if (myBuffer == null) log.error("BUFFER NULL for {} ... wont be writing packets", remoteAgentPort);
              else myBuffer.incomingPacket((ByteBuf) msg);

            totalBytes += ((ByteBuf) msg).capacity();
            // do we need to release this msg also .. cause we are copying it in hashmap
           // ReferenceCountUtil.release(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.flush();

            long stopTime = System.currentTimeMillis();
            log.info("Agentserver rate {}", (totalBytes * 8)/(stopTime-startTime)/1000);

            myEndHost.transferCompleted(); // notify the host server

            hostManager.removeAgentToHost(myEndHost);
            bufferManager.removeBuffer(myBuffer);

            ctx.close(); //close this channel
            log.debug("Channel is inactive... Closing it");
            StatCollector.getStatCollector().connectionRemoved();

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
                                                  .addLast("lengthdecorder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                                  // .addLast("bytesDecoder", new ByteArrayDecoder())
                                                  .addLast(new AgentServerHandler())
                                                  .addLast("4blength", new LengthFieldPrepender(4))
                                                //  .addLast("bytesEncoder", new ByteArrayEncoder())
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

    private RequestTemplateWrapper getMyRequestByClientAgentPort(String remoteIP, int remotePort) {
        for (RequestTemplateWrapper incomingRequest : incomingRequests) {
            if (incomingRequest.getRequest().getClientAgentIP().equals(remoteIP)) {
                for (int port : incomingRequest.getPorts()
                        ) {
                    if (port == remotePort) {
                        deleteRequestFromList(incomingRequest); // also delete this request from pool. we wont be needing it.
                        return incomingRequest;
                    }
                }
            }
        }
        return null;
    }

    private void deleteRequestFromList(RequestTemplateWrapper request) {
        incomingRequests.remove(request);
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


}
