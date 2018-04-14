package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.buf.OrderedPacketListener;
import edu.clemson.openflow.sos.host.HostClient;
import edu.clemson.openflow.sos.host.HostStatusInitiator;
import edu.clemson.openflow.sos.host.HostStatusListener;
import edu.clemson.openflow.sos.rest.RequestMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class AgentToHost implements OrderedPacketListener, HostPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AgentToHost.class);
    private final HostStatusInitiator hostStatusInitiator;

    private RequestMapper request;
    private ArrayList<Channel> channels;
    private HostClient hostClient;
    private int currentChannelNo = 0;


    public AgentToHost(RequestMapper request) {
        this.request = request;
        channels = new ArrayList<>();
        hostClient = new HostClient();
        hostClient.setListener(this);

        hostStatusInitiator = new HostStatusInitiator();
        hostStatusInitiator.addListener(hostClient);

        hostClient.start(request.getRequest().getServerIP(), request.getRequest().getServerPort());
        log.debug("Created & started new host handler for server {} port {}",
                request.getRequest().getServerIP(),
                request.getRequest().getServerPort());
    }

    @Override
    public void orderedPacket(ByteBuf packet) { //TODO: remove incoming request
        log.debug("Got new sorted packet");
        byte[] bytes = new byte[packet.capacity() - 4 ];
        packet.getBytes(4, bytes);
        ChannelFuture cf = hostClient.getMyChannel().writeAndFlush(bytes);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                ReferenceCountUtil.release(packet);
            }
        });
        //ReferenceCountUtil.release(bytes);
      //  ReferenceCountUtil.release(packet);
        //  if (!cf.isSuccess()) log.error("write not successful {}", cf.cause());
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

        RequestMapper host = (RequestMapper) o;

        if (request.getRequest().getServerPort() != host.getRequest().getServerPort()) return false;
        return request.getRequest().getServerIP().equals(host.getRequest().getServerIP());
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public void hostPacket(byte[] packet) {
        if (currentChannelNo == channels.size()) currentChannelNo = 0;
        log.debug("Forwarding packet with size {} on channel {} to Agent-Client", packet.length, currentChannelNo);
     //   log.info(packet.length + "");
        channels.get(currentChannelNo).writeAndFlush(packet);
        currentChannelNo ++ ;
    }

    public void transferCompleted() {
        hostStatusInitiator.hostStatusChanged(HostStatusListener.HostStatus.DONE);
    }
}