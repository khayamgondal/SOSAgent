package edu.clemson.openflow.sos.buf;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.agent.netty.AgentClient;
import edu.clemson.openflow.sos.rest.AgentPortMapper;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import edu.clemson.openflow.sos.rest.RestRoutes;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
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
import java.util.List;

/**
 * @author This class will <Mul/DeMul> receive incoming packets from connected client's  &
 * will append the sequence no. and will sent the data out on no. of parallel connections.
 */
public class Multiplexer {
    private static final Logger log = LoggerFactory.getLogger(Multiplexer.class);
    private static final String PORTMAP_PATH = "/portmap";
    private static final String REST_PORT = "8001";
    private byte seqNo = -128;
    private int currentChannelNo = 0;
    private ControllerRequestMapper request;
    private ArrayList<Channel> channels;

    public Multiplexer(ControllerRequestMapper request) {
        this.request = request;
        channels = new ArrayList<>(request.getNumParallelSockets());
        AgentClient agentClient = new AgentClient();
        EventLoopGroup eventLoopGroup = agentClient.createEventLoopGroup();
        log.debug("Bootstrapping {} connections to agent server", request.getNumParallelSockets());
        for (int i=0; i < request.getNumParallelSockets(); i++)
            channels.add(agentClient.bootStrap(eventLoopGroup, request.getServerAgentIP()));
        // TODO: Notify the agent-server about the ports so It can use it to filter out

        List<Integer> ports = new ArrayList<>(request.getNumParallelSockets());
        for (Channel channel: channels
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

    public void incomingPacket(byte[] packet) {
        if (currentChannelNo == request.getNumParallelSockets()) currentChannelNo = 0;
        if (seqNo == 127) seqNo = 0;
        ByteBuffer toSend = ByteBuffer.allocate(1 + packet.length).put(seqNo).put(packet);

        writeToAgentChannel(channels.get(currentChannelNo), toSend.array());
        log.debug("Wrote packet with size {} & seq {} on channel no {}", packet.length, seqNo, currentChannelNo);

        currentChannelNo ++;
        seqNo ++;
    }

    private void writeToAgentChannel(Channel channel, byte[] data) {
        channel.writeAndFlush(data);

    }
    //TODO: apache is deprecated webclient
    private boolean notifyRemoteAgent(List<Integer> ports) throws IOException {
        String uri = RestRoutes.URIBuilder(request.getServerAgentIP(), REST_PORT, PORTMAP_PATH);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost(uri);

        AgentPortMapper portMap = new AgentPortMapper(request, ports); //portmap contains both controller request and all the associated portss
        ObjectMapper mapperObj = new ObjectMapper();
        String portMapString = mapperObj.writeValueAsString(portMap);

        org.apache.http.entity.StringEntity stringEntry = new org.apache.http.entity.StringEntity(portMapString, "UTF-8");
        httpRequest.setEntity(stringEntry);
        log.debug("JSON Object to sent {}", portMapString);
        HttpResponse response = httpClient.execute(httpRequest);

        log.info("Sending HTTP request to remote agent with port info{}", request.getServerAgentIP());
        log.debug("Agent returned {}", response.toString());
        return Boolean.parseBoolean(response.toString());
    }
}
