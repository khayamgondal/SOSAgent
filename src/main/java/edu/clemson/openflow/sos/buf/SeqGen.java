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

    private int seqNo = 0;
    private static final int MAX_SEQ = Integer.MAX_VALUE;
    // private ControllerRequestMapper request;
    //  private ArrayList<Channel> channels;
    private AgentClient agentClient;

  //  public SeqGen(ControllerRequestMapper request) {
        //    this.request = request;
        //   channels = new ArrayList<>(request.getNumParallelSockets());

      //  agentClient = new AgentClient(request);
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
  //  }

    //TODO: check performance when instead of byteBuffer bytebuf is used
    public byte[] incomingPacket(byte[] packet) {
        //  if (currentChannelNo == request.getNumParallelSockets()) currentChannelNo = 0;
        if (seqNo == MAX_SEQ) seqNo = 0;

        //ByteBuffer toSend = ByteBuffer.allocate(Integer.SIZE + Integer.SIZE + packet.length)
        //        .putInt(seqNo).putInt(packet.length).put(packet);
        ByteBuffer toSend = ByteBuffer.allocate(4 + packet.length);
        toSend.putInt(seqNo)
                .put(packet);
        log.debug("prepended seq no. {}.. ", toSend.getInt(0));
        //writeToAgentChannel(channels.get(currentChannelNo), toSend.array());


        //agentClient.incomingPacket(toSend.array());
        seqNo++;
        return toSend.array();
    }

    private void writeToAgentChannel(Channel channel, byte[] data) {
        channel.writeAndFlush(data);

    }

}
