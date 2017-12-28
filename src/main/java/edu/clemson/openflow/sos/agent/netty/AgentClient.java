package edu.clemson.openflow.sos.agent.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.agent.DataPipelineInitiator;
import edu.clemson.openflow.sos.agent.DataPipelineListener;
import edu.clemson.openflow.sos.agent.OrderedPacketListener;
import edu.clemson.openflow.sos.buf.Buffer;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import edu.clemson.openflow.sos.rest.RestRoutes;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentClient implements OrderedPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);

    private static final String PORTMAP_PATH = "/portmap";
    private static final String REST_PORT = "8002";
    private static final int AGENT_DATA_PORT = 9878;

    private int currentChannelNo = 0;

    //private Channel myChannel;
    private DataPipelineInitiator dataPipelineInitiator;
    private AgentClientHandler agentClientHandler;
    private IncomingRequestMapper request;
    private ArrayList<Channel> channels;
    private Buffer myBuffer;
    private Channel hostChannel;

    @Override
    public void orderedPacket(ByteBuf packet, IncomingRequestMapper request) {
        byte[] bytes = new byte[packet.capacity() - 4 ];
        packet.getBytes(4, bytes);
        ChannelFuture cf = hostChannel.writeAndFlush(bytes);

      //  if (!cf.isSuccess()) log.error("write back to host not successful {}", cf.cause());

    }

    public void setChannel(Channel channel) {
        this.hostChannel = channel;
    }

    public class AgentClientHandler extends ChannelInboundHandlerAdapter {


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("Reading from remote agent");
            myBuffer.incomingPacket(Unpooled.wrappedBuffer((byte[]) msg));
          //  dataPipelineInitiator.packetArrived(msg); // send back to host side
        }

    }

    public AgentClient(IncomingRequestMapper request) {
        agentClientHandler = new AgentClientHandler();
        this.request = request;
        channels = new ArrayList<>(request.getRequest().getNumParallelSockets());
        myBuffer = new Buffer();
        myBuffer.setListener(this);
        EventLoopGroup eventLoopGroup = createEventLoopGroup();
        log.debug("Bootstrapping {} connections to agent server", request.getRequest().getNumParallelSockets());
        for (int i = 0; i < request.getRequest().getNumParallelSockets(); i++)
            channels.add(bootStrap(eventLoopGroup, request.getRequest().getServerAgentIP()));
        // TODO: Notify the agent-server about the ports so It can use it to filter out

        List<Integer> ports = new ArrayList<>(request.getRequest().getNumParallelSockets());
        for (Channel channel : channels
                ) {
            InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
            ports.add(socketAddress.getPort());
        }
        try {
            boolean remoteAgentRes = notifyRemoteAgent(ports);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: apache is deprecated webclient...
    private boolean notifyRemoteAgent(List<Integer> ports) throws IOException {
        String uri = RestRoutes.URIBuilder(request.getRequest().getServerAgentIP(), REST_PORT, PORTMAP_PATH);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost(uri);

        IncomingRequestMapper portMap = new IncomingRequestMapper(request.getRequest(), ports); //portmap contains both controller request and all the associated portss
        ObjectMapper mapperObj = new ObjectMapper();
        String portMapString = mapperObj.writeValueAsString(portMap);

        org.apache.http.entity.StringEntity stringEntry = new org.apache.http.entity.StringEntity(portMapString, "UTF-8");
        httpRequest.setEntity(stringEntry);
        log.debug("JSON Object to sent {}", portMapString);
        HttpResponse response = httpClient.execute(httpRequest);

        log.info("Sending HTTP request to remote agent with port info{}", request.getRequest().getServerAgentIP());
        log.debug("Agent returned {}", response.toString());
        return Boolean.parseBoolean(response.toString());
    }


    public void incomingPacket(byte[] data) {
        if (currentChannelNo == request.getRequest().getNumParallelSockets()) currentChannelNo = 0;
        log.debug("Trying to write packet with size {} & seq {} on channel no {}", data.length,
                ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 31)).getInt(),
                currentChannelNo);
        writeToAgentChannel(channels.get(currentChannelNo), data);
        currentChannelNo++;
    }

    private void writeToAgentChannel(Channel channel, byte[] data) {
       // log.debug("packet content is {}", new String(data));
        ChannelFuture cf = channel.writeAndFlush(data);
      /*  if (!cf.isSuccess()) {
            log.error("Sending packet failed .. due to {}", cf.cause());
        }*/
    }


    private Channel bootStrap(EventLoopGroup group, String agentServerIP) {
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast("lengthdecorder",
                                            new LengthFieldBasedFrameDecoder(65548, 0, 4, 0, 4))
                                    .addLast("4blengthdec", new ByteArrayDecoder())
                                    .addLast("agentClient", new AgentClientHandler())
                                    .addLast("4blength", new LengthFieldPrepender(4))
                                    .addLast("bytesEncoder", new ByteArrayEncoder());
                        }
                    });
            ;

            Channel myChannel = bootstrap.connect(agentServerIP, AGENT_DATA_PORT).sync().channel();
            //if (myChannel == null) log.debug("in start it is nul");
            log.debug("Connected to Agent-Server {} on Port {}", agentServerIP, AGENT_DATA_PORT);
            return myChannel;

        } catch (Exception e) {
            log.error("Error connecting to Agent-Server {} on Port{}", agentServerIP, AGENT_DATA_PORT);
            e.printStackTrace();
            return null;
        } finally {
            //group.shutdownGracefully();
        }
    }

    private NioEventLoopGroup createEventLoopGroup() {
        return new NioEventLoopGroup();
    }
   // public void start(String agentServerIP) {
   //     EventLoopGroup eventLoopGroup = createEventLoopGroup();
        //bootStrap(eventLoopGroup, agentServerIP);
   // }

   // public Channel bootStrap(EventLoopGroup eventLoopGroup, String remoteIP) {
   //     return bootStrap(eventLoopGroup, remoteIP);
   // }

   //@Override
    public void hostConnected(ControllerRequestMapper request, Object callBackObject) {
  /*       dataPipelineInitiator = new DataPipelineInitiator();
        dataPipelineInitiator.addListener((HostServer) callBackObject);
        log.debug("new client connection from host {} port {}", request.getClientAgentIP(), request.getClientPort());
        start(request.getServerAgentIP());
  */  }

 //   @Override
    public void packetArrived(Object msg) { //write this packet
   /*     log.debug("Received new packet from host");
        if (myChannel == null) log.error("Current channel is null, wont be forwarding packet to other agent");
        else {
            log.debug("Forwarding to {}", myChannel.remoteAddress().toString());
            //   byte[] d = (byte[]) msg;
            //   String s = new String(d);
            //   log.debug("KKK {}", s);
            myChannel.writeAndFlush(msg);
        }*/
    }

   // @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }
}
