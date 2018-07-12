package edu.clemson.openflow.sos.host;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.agent.AgentClient;
import edu.clemson.openflow.sos.buf.SeqGen;
import edu.clemson.openflow.sos.manager.ControllerManager;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.rest.RequestListenerInitiator;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.shaping.HostTrafficShaping;
import edu.clemson.openflow.sos.shaping.RestStatListener;
import edu.clemson.openflow.sos.utils.MappingParser;
import edu.clemson.openflow.sos.utils.MockRequestBuilder;
import edu.clemson.openflow.sos.utils.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 * Listens for connections from client on port 9877
 */

public class HostServer extends ChannelInboundHandlerAdapter implements ISocketServer,
        RestStatListener, RequestListener {

    private static final Logger log = LoggerFactory.getLogger(HostServer.class);

    private static final int DATA_PORT = 9877;

    private List<RequestTemplateWrapper> incomingRequests = new ArrayList<>();
    private NioEventLoopGroup group;
    private HostStatusInitiator hostStatusInitiator;
    private HostTrafficShaping hostTrafficShaping;

    private RequestListenerInitiator requestListenerInitiator;

    private long totalWritten;

    private boolean mockRequest;
    private int mockParallelConns;
    private List<MappingParser> mockMapping;
    ObjectMapper mapper = new ObjectMapper();

    public HostServer() {

        requestListenerInitiator = new RequestListenerInitiator();
        requestListenerInitiator.addRequestListener(this);

        //  Utils.requestListeners.add(this); //we register for incoming requests

        if (Utils.router != null) {
            Utils.router.getContext().getAttributes().put("host-callback", requestListenerInitiator);
            Utils.router.getContext().getAttributes().put("callback", this); //also pass the callback listener via router context
        }
        if (Utils.configFile != null) {
            mockRequest = Boolean.parseBoolean(Utils.configFile.getProperty("test_mode"));
            mockParallelConns = Integer.parseInt(Utils.configFile.getProperty("test_conns").replaceAll("[\\D]", ""));
        }
        try {
            mockMapping = mapper.readValue(Utils.configFile.getProperty("test_mapping"), new TypeReference<List<MappingParser>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println(mockMapping.toArray().toString() );
    }

    private synchronized void totalWritten(long written) {
        totalWritten += written;
        //   log.info(" {}", totalWritten * 8 /1024);

    }

    @Override
    public void RestStats(double totalReadThroughput, double totalWriteThroughput) {
        //    if (hostTrafficShaping != null) hostTrafficShaping.setReadLimit((long) totalReadThroughput);
        log.info("Remote Agent Read {} Gbps", totalReadThroughput * 8 / 1024 / 1024 / 1024);
    }


    public class HostServerHandler extends ChannelInboundHandlerAdapter {

        private ControllerManager controllerManager;
        private float totalBytes;
        private long startTime;


        private RequestTemplateWrapper request;
        private SeqGen seqGen;
        private AgentClient agentClient;

        private int myMockIndex(String localAgentIP) {
            for (int i = 0; i < mockMapping.size(); i++)
                if (mockMapping.get(i).getClientAgentIP().equals(localAgentIP)) return i;
            return -1;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            InetSocketAddress remoteSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            InetSocketAddress localSocketAddress = (InetSocketAddress) ctx.channel().localAddress();
            String hName = "172.0.0.111";
            String l = "172.0.0.11";
            log.info("New host-side connection from {} at Port {}",
                    remoteSocketAddress.getHostName(),
                    remoteSocketAddress.getPort());

            if (mockRequest) {
              // int myIndex = myMockIndex(localSocketAddress.getHostName());
               int  myIndex = myMockIndex(l);

                if (myIndex == -1) {
                    log.error("Couldn't find entry for this agent in config.properties..");
                    return;
                }
            /*    request = new MockRequestBuilder().buildRequest(remoteSocketAddress.getHostName(), remoteSocketAddress.getPort(),
                        localSocketAddress.getHostName(), mockMapping.get(myIndex).getServerAgentIP(), mockParallelConns, 1,
                        mockMapping.get(myIndex).getServerIP(), mockMapping.get(myIndex).getServerPort());*/
                request = new MockRequestBuilder().buildRequest(hName, remoteSocketAddress.getPort(),
                       l, mockMapping.get(myIndex).getServerAgentIP(), mockParallelConns, 1,
                      mockMapping.get(myIndex).getServerIP(), mockMapping.get(myIndex).getServerPort());
            }
            //TODO: If remotely connecting client is in your /etc/hosts than remoteSocketAddress.getHostName() will return that hostname instead of its IP address and following method call will return null
            else
              //  request = getClientRequest(remoteSocketAddress.getHostName(), remoteSocketAddress.getPort()); // go through the list and find related request
             request = getClientRequest(hName, remoteSocketAddress.getPort()); // go through the list and find related request

            if (request == null) {
                log.error("No controller request found for this associated port ...all incoming packets will be dropped ");
                return;
            }
            startTime = System.currentTimeMillis();

            if (request != null) {
                seqGen = new SeqGen();

                agentClient = new AgentClient(request);
                agentClient.bootStrapSockets();
                agentClient.setWriteBackChannel(ctx.channel());

                hostStatusInitiator = new HostStatusInitiator();
                hostStatusInitiator.addListener(agentClient);

                controllerManager = new ControllerManager(request.getRequest().getTransferID(),
                        request.getRequest().getControllerIP());
            } else log.error("Couldn't find the request {} in request pool. Not notifying agent",
                    request.toString());

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            //    log.info("Read limit {}", hts.getReadLimit());

            if (request != null && seqGen != null) {
                ByteBuf seqed = seqGen.incomingPacket((byte[]) msg);
                agentClient.incomingPacket(seqed);
                totalBytes += ((byte[]) msg).length;
            } else {
                log.error("Couldn't find the request. Not forwarding packet");
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.flush();
            log.info("Client is done sending");
            if (hostStatusInitiator != null)
                hostStatusInitiator.hostStatusChanged(HostStatusListener.HostStatus.DONE); // notify Agent Client that host is done sending

            long stopTime = System.currentTimeMillis();
            log.info("HostServer rate {}", (totalBytes * 8) / (stopTime - startTime) / 1000000);
            // also notify controller to tear down this connection.
            //    if (!request.getRequest().isMockRequest()) controllerManager.sendTerminationMsg();
        }

    }


    /*
        @param perChannel per channel write rate in Mbps
        @return read limit in bytes per second
     */
   /* private long channelToGlobalReadLimit(int perChannel) {
        return perChannel * request.getRequest().getNumParallelSockets() * 1024 *1024 / 8;
    }*/

    private boolean startSocket(int port) {
        group = new NioEventLoopGroup();

        hostTrafficShaping = new HostTrafficShaping(group, 0, 00000000, 5000);

        //  ShapingTimer timer = new ShapingTimer(hostTrafficShaping);
        //  ScheduledExecutorService scheduledExecutorService =
        //          Executors.newScheduledThreadPool(1);
        //  scheduledExecutorService.scheduleAtFixedRate(timer, 0, 10, TimeUnit.SECONDS);


        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(2048 * 1024, 2048 * 1024))
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer() {
                                      @Override
                                      protected void initChannel(Channel channel) throws Exception {
                                          //can I remove bytes decorder and get bytebuf?
                                          channel.pipeline()
                                                  .addLast("traffic", hostTrafficShaping)
                                                  .addLast("bytesDecoder", new ByteArrayDecoder())
                                                  .addLast("hostHandler", new HostServerHandler())
                                                  .addLast("bytesEncoder", new ByteArrayEncoder());

                                      }
                                  }
                    );
            ChannelFuture f = b.bind().sync();
            log.info("Started host-side socket server at Port {}", port);
            return true;
        } catch (InterruptedException e) {
            log.error("Error starting host-side socket");
            e.printStackTrace();
            return false;
        }
    }

    private RequestTemplateWrapper getClientRequest(String remoteIP, int remotePort) {
        //Controller sends client port in request msg. If we are using mock client, there is no way to know the port before actually
        //starting socket so for now I am just skipping the port check
        //TODO: may be first send the packet and than request ? doesn't look like a good idea though :p
        // if its a mock request we dont need to match the port.. just match on IP
        for (RequestTemplateWrapper incomingRequest : incomingRequests) {
            if (!incomingRequest.getRequest().isMockRequest() ?
                    incomingRequest.getRequest().getClientIP().equals(remoteIP) && incomingRequest.getRequest().getClientPort() == remotePort :
                    incomingRequest.getRequest().getClientIP().equals(remoteIP))
                // if (incomingRequest.getRequest().getClientIP().equals(remoteIP) &&
                //        incomingRequest.getRequest().getClientPort() == remotePort)
                return incomingRequest;
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
    public void newIncomingRequest(RequestTemplateWrapper request) {
        incomingRequests.add(request);

    }
}
