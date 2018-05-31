package edu.clemson.openflow.sos.agent;

/**
 * @author khayam anjam kanjam@g.clemson.edu
 * This class receives data form AgentClient and writes into Buffer
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.buf.*;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.rest.RestRoutes;
import edu.clemson.openflow.sos.shaping.AgentTrafficShaping;
import edu.clemson.openflow.sos.shaping.IStatListener;
import edu.clemson.openflow.sos.shaping.StatsTemplate;
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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class AgentServer implements ISocketServer, IStatListener {

    private static final Logger log = LoggerFactory.getLogger(AgentServer.class);

    private static final String TRAFFIC_PATH = "/traffic";
    private static final String REST_PORT = "8002";
    private static final int AGENT_DATA_PORT = 9878;

    private BufferManager bufferManager;
    private AgentToHostManager hostManager;

    private List<RequestTemplateWrapper> incomingRequests;
    private NioEventLoopGroup group;


    private int gotStatsFrom;
    private double totalReadThroughput, totalWriteThroughput; //also need to reset these after we send these back to other agents and before
    // we have new stats available. Also keep track of total open parallel connections to findout if we have received stats from all connections.


    public AgentServer() {
        incomingRequests = new ArrayList<>();
        bufferManager = new BufferManager(); //setup buffer manager.
        hostManager = new AgentToHostManager();
    }

    @Override
    public void notifyStats(long lastWriteThroughput, long lastReadThroughput) {
        gotStatsFrom ++;
        sumThroughput(lastWriteThroughput, lastReadThroughput);
        if (gotStatsFrom == StatCollector.getStatCollector().getTotalOpenedConnections()) { //mean now we have gotten stats from all conns. time to notify other agents
            log.info("Notifying client-agent about stats ");
            try {
                notifyAgents();
            } catch (IOException e) {
                e.printStackTrace();
            }
            gotStatsFrom = 0;
            totalReadThroughput = 0;
            totalWriteThroughput = 0;

        }

    }

    private void notifyAgents() throws IOException {
        for (RequestTemplateWrapper request: incomingRequests
             ) {
            String uri = RestRoutes.URIBuilder(request.getRequest().getClientAgentIP(), REST_PORT, TRAFFIC_PATH);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpRequest = new HttpPost(uri);

            StatsTemplate statsTemplate = new StatsTemplate(totalWriteThroughput, totalReadThroughput);

            ObjectMapper mapperObj = new ObjectMapper();
            String portMapString = mapperObj.writeValueAsString(statsTemplate);

            org.apache.http.entity.StringEntity stringEntry = new org.apache.http.entity.StringEntity(portMapString, "UTF-8");
            httpRequest.setEntity(stringEntry);
            log.debug("JSON Object to sent {}", portMapString);
            HttpResponse response = httpClient.execute(httpRequest);

            log.debug("Sending HTTP request to remote agent with port info {}", request.getRequest().getServerAgentIP());
            log.debug("Agent returned {}", response.getStatusLine().getStatusCode());
        }
    }

    private synchronized void sumThroughput(long lastWriteThroughput, long lastReadThroughput) {
        totalReadThroughput += lastReadThroughput;
        totalWriteThroughput += lastWriteThroughput;
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

                    addToRequestPool(request); // also remove this request once connection terminates. TODO
                    myEndHost = hostManager.addAgentToHost(request);
                    myEndHost.addChannel(myChannel);
                    myBuffer = bufferManager.addBuffer(request, myEndHost); //passing callback listener so when sorted packets are avaiable it can notify the agent2host
                }
            }

        private void addToRequestPool(RequestTemplateWrapper request) {
            if (!incomingRequests.contains(request)) incomingRequests.add(request);
        }


        private void removeFromRequestPool(RequestTemplateWrapper request) {
            if (incomingRequests.contains(request)) incomingRequests.remove(request);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
        AgentTrafficShaping ats = new AgentTrafficShaping( group, 10000);
        ats.setStatListener(this);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer() {
                                      @Override
                                      protected void initChannel(Channel channel) throws Exception {
                                          channel.pipeline()
                                                  .addLast("agent-traffic-shapping", ats)
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
