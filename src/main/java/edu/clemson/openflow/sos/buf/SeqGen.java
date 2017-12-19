package edu.clemson.openflow.sos.buf;

import edu.clemson.openflow.sos.agent.netty.AgentClient;
import edu.clemson.openflow.sos.rest.ControllerRequestMapper;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This class will append the sequence no. and will forward formed packet to AgentClient
 */
public class SeqGen {
    private static final Logger log = LoggerFactory.getLogger(SeqGen.class);

    private byte seqNo = -128;
   // private ControllerRequestMapper request;
  //  private ArrayList<Channel> channels;
    private AgentClient agentClient;

    public SeqGen(ControllerRequestMapper request) {
    //    this.request = request;
     //   channels = new ArrayList<>(request.getNumParallelSockets());

        agentClient = new AgentClient(request);
       /* EventLoopGroup eventLoopGroup = agentClient.createEventLoopGroup();
        log.debug("Bootstrapping {} connections to agent server", request.getNumParallelSockets());
        for (int i = 0; i < request.getNumParallelSockets(); i++)
            channels.add(agentClient.bootStrap(eventLoopGroup, request.getServerAgentIP()));
        // TODO: Notify the agent-server about the ports so It can use it to filter out

        List<Integer> ports = new ArrayList<>(request.getNumParallelSockets());
        for (Channel channel : channels
                ) {
            InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
            ports.add(socketAddress.getPort());
        }
        try {
            boolean remoteAgentRes = agentClient.notifyRemoteAgent(ports);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void incomingPacket(byte[] packet) {
      //  if (currentChannelNo == request.getNumParallelSockets()) currentChannelNo = 0;
        if (seqNo == 127) seqNo = 0;
        ByteBuffer toSend = ByteBuffer.allocate(1 + packet.length).put(seqNo).put(packet);

        //writeToAgentChannel(channels.get(currentChannelNo), toSend.array());
        agentClient.incomingPacket(seqNo, toSend.array());

        seqNo++;
    }

    private void writeToAgentChannel(Channel channel, byte[] data) {
        channel.writeAndFlush(data);

    }

}
