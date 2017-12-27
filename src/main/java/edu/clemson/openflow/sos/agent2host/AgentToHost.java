package edu.clemson.openflow.sos.agent2host;

import edu.clemson.openflow.sos.agent.OrderedPacketListener;
import edu.clemson.openflow.sos.host.netty.HostClient;
import edu.clemson.openflow.sos.host.netty.HostServer;
import edu.clemson.openflow.sos.rest.IncomingRequestMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class AgentToHost implements OrderedPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AgentToHost.class);

    private IncomingRequestMapper request;
    private ArrayList<Channel> channels;
    private HostClient hostClient;

    public AgentToHost(IncomingRequestMapper request) {
        this.request = request;
        channels = new ArrayList<>();
        hostClient = new HostClient();
        hostClient.start(request.getRequest().getServerIP(), request.getRequest().getServerPort());
        log.debug("Created & started new host handler for server {} port {}",
                request.getRequest().getServerIP(),
                request.getRequest().getServerPort());
    }

    @Override
    public void orderedPacket(ByteBuf packet, IncomingRequestMapper request) {
        log.debug("Got new sorted packet");
        ByteBuf des;
        byte[] bytes = new byte[packet.capacity() - 4 ];
        packet.getBytes(4, bytes);
        ChannelFuture cf = hostClient.getMyChannel().writeAndFlush(bytes);

        if (!cf.isSuccess()) log.error("write not successful {}", cf.cause());
    }
    public void addChannel(Channel channel) {
        channels.add(channel);
        log.debug("added channel for client {} : {} server {} : {}",
                request.getRequest().getClientIP(),
                request.getRequest().getClientPort(),
                request.getRequest().getServerIP(),
                request.getRequest().getServerPort());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
      //  if (o == null || getClass() != o.getClass()) return false;

        IncomingRequestMapper host = (IncomingRequestMapper) o;

        if (request.getRequest().getServerPort() != host.getRequest().getServerPort()) return false;
        return request.getRequest().getServerIP().equals(host.getRequest().getServerIP());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}