package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.agent.HostStatusInitiator;
import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.agent.IncomingRequestListener;
import edu.clemson.openflow.sos.buf.*;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.manager.IncomingRequestManager;
import edu.clemson.openflow.sos.rest.IncomingRequestHandler;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import edu.clemson.openflow.sos.utils.EventListenersLists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

public class AgentServer implements ISocketServer, HostStatusListener, IncomingRequestListener {
    private static final int AGENT_DATA_PORT = 9878;
    private static final Logger log = LoggerFactory.getLogger(AgentServer.class);
    //private ControllerRequestMapper request;
    private Channel myChannel;
    private HostStatusInitiator hostStatusInitiator;
    private IncomingRequestMapper request;
    //private RequestPool requestPool;
    private Demultiplexer demultiplexer;

    private PacketFilter packetFilter;
    private BufferManager bufferManager;
    private Buffer myBuffer;

    private List<IncomingRequestMapper> incomingRequests;
    private List<PacketBuffer> packetBuffers = new ArrayList<>();

    public AgentServer() {
        incomingRequests = new ArrayList<>();
        EventListenersLists.incomingRequestListeners.add(this);
        bufferManager = new BufferManager(); //setup buffer manager.
    }

    public class AgentServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            log.info("New agent-side connection from agent {} at Port {}",
                    socketAddress.getHostName(),
                    socketAddress.getPort());


            //RequestManager requestManager = RequestManager.INSTANCE;
            //iterate over all the received requests and find the one which matches this port. It
            //Optional<IncomingRequestMapper> incomingRequest = IncomingRequestManager.INSTANCE.getRequestByPort(socketAddress.getPort());
            //if (! incomingRequest.isPresent()) {
            //    log.error("No request found using this port");
            //    return;
            //    }

           request = getMyRequestByClientAgentPort(socketAddress.getHostName(), socketAddress.getPort()); // go through the list and find related request
            if (request == null) {
                log.error("No controller request found for this associated port ...all incoming packets will be dropped ");
                return;
            }
         /*   PacketBuffer packetBuffer = getMyPacketBuffer(request);
            if (packetBuffer == null) {
                log.error("No allocated packet buffer for this request found... returning ...");
                return;
            }*/
            //request = requestManager.getRequest(socketAddress.getHostName(),
            //      socketAddress.getPort(), false);

           // packetFilter = new PacketFilter(request);
            myBuffer = bufferManager.addBuffer(request);
            myChannel = ctx.channel();

            // if (request != null) {
            //hostStatusInitiator = new HostStatusInitiator();
            //HostClient hostClient = new HostClient(); // we are passing our channel to HostClient so It can write back the response messages

            //hostStatusInitiator.addListener(hostClient);
            //hostStatusInitiator.hostConnected(request, this); //also pass the call back handler so It can respond back
            //   }
            //   else log.error("Couldn't find the request {} in request pool. wont be acting",
            //           request.toString());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {

            if (request == null) {
                ReferenceCountUtil.release(msg);
                log.debug("No request found .. releasing received packets");
                return;
            }
            //ByteBuf bytes = Unpooled.wrappedBuffer((byte[]) msg); //PERFORMANCE
            log.debug( ""+((byte[]) msg).length);
            //log.debug("Got packet with seq {} & size {}" ,bytes.getInt(0),
            //        bytes.getInt(31));
            //myBuffer.incomingPacket(bytes);

            //packetFilter.packetToFilter(bytes);
           // log.debug("seq no: is {}", bytes[0] & 0xff);
            //  if (request != null) {
            //     hostStatusInitiator.packetArrived(msg); //notify handlers
            // }
            // else log.error("Couldn't find the request {} in request pool. " +
            //        "Not forwarding packet", request.toString());
            ReferenceCountUtil.release(msg);
        }
    }

    /*   public AgentServer(RequestPool requestPool) {
           this.requestPool = requestPool;
       }
   */
    private boolean startSocket(int port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer() {
                                      @Override
                                      protected void initChannel(Channel channel) throws Exception {
                                          channel.pipeline().addLast("bytesDecoder",
                                                  new LengthFieldBasedFrameDecoder());
                                          channel.pipeline().addLast(
                                                  new AgentServerHandler());
                                          channel.pipeline().addLast("bytesEncoder", new ByteArrayEncoder());
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

    private PacketBuffer getMyPacketBuffer(IncomingRequestMapper request) {
        for (PacketBuffer buffer : packetBuffers
                ) {
            if (buffer.getRequest().equals(request)) return buffer;
        }
        return null;
    }

    private IncomingRequestMapper getMyRequestByClientAgentPort(String remoteIP, int remotePort) {
        for (IncomingRequestMapper incomingRequest : incomingRequests) {
            if (incomingRequest.getRequest().getClientAgentIP().equals(remoteIP)) {
                for (int port : incomingRequest.getPorts()
                        ) {
                    if (port == remotePort) return incomingRequest;
                }
            }
            //incomingRequest.getPorts().stream().filter(o -> o.equals(socketAddress.getPort())).findFirst().isPresent();
        }
        return null;
    }

    @Override
    public boolean start() {
        return startSocket(AGENT_DATA_PORT);
    }

    @Override
    public void hostConnected(ControllerRequestMapper request, Object hostStatusInitiater) {

    }

    @Override
    public void packetArrived(Object msg) {
        log.debug("Received new packet from host sending back to agent");
        if (myChannel == null) log.error("Current context is null, wont be sending packet back to host");
        else myChannel.writeAndFlush(msg);
    }

    @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }

    @Override
    public void newIncomingRequest(IncomingRequestMapper request) {
        incomingRequests.add(request);
       // packetBuffers.add(packetBuffer);
        log.debug("Received new request from client agent {}", request.toString());
    }
}
