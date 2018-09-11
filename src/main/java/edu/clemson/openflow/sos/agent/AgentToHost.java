package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.buf.Buffer;
import edu.clemson.openflow.sos.buf.OrderedPacketListener;
import edu.clemson.openflow.sos.host.HostClient;
import edu.clemson.openflow.sos.host.HostStatusInitiator;
import edu.clemson.openflow.sos.host.HostStatusListener;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.stats.StatCollector;
import edu.clemson.openflow.sos.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class AgentToHost implements OrderedPacketListener, HostPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AgentToHost.class);
    private final HostStatusInitiator hostStatusInitiator;

    private RequestTemplateWrapper request;
    private ArrayList<Channel> channels;

    private HostPacketInitiator hostPacketInitiator;
    private HostClient hostClient;
    private Buffer buffer;

    private int currentChannelNo = 0;
    private long totalBytes, startTime, endTime;
    private int wCount;
    private long writableCount, unwritableCount;
    int shouldSend = 0;



    public AgentToHost(RequestTemplateWrapper request) {
        this.request = request;
        channels = new ArrayList<>();


        hostClient = new HostClient();
        hostPacketInitiator = new HostPacketInitiator();
        hostPacketInitiator.addListener(this);
        hostClient.setHostPacketListenerInitiator(hostPacketInitiator);

        hostStatusInitiator = new HostStatusInitiator();
        hostStatusInitiator.addListener(hostClient);

        hostClient.start(request.getRequest().getServerIP(), request.getRequest().getServerPort());
        log.debug("Created & started new host handler for server {} port {}",
                request.getRequest().getServerIP(),
                request.getRequest().getServerPort());
        startTime = System.currentTimeMillis();

    }


    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean orderedPacket(ByteBuf packet) {
         if (!sendToHost(packet))
             packet.release();
         return true;
         //return sendToHost(packet); //This is real
    }

    private boolean sendToHost(ByteBuf packet) {
     //   smartSend(packet);
        //TODO: @smartSend().. separate write and flush
        if (hostClient.getHostChannel().isWritable()) {
           // hostClient.getHostChannel().writeAndFlush(packet.slice(4, packet.capacity() - 4));
            hostClient.getHostChannel().writeAndFlush(packet);
            writableCount++;
            return true;
        } else {
            unwritableCount++;
            return false;
        }
    }

    private void smartSend(ByteBuf packet) {
        if (hostClient.getHostChannel().isWritable()) {
            hostClient.getHostChannel().write(packet);
            writableCount++;
        }
       else { packet.release(); unwritableCount++; }
        shouldSend++;

        if (shouldSend > request.getRequest().getQueueCapacity()) {
            hostClient.getHostChannel().flush();
            shouldSend = 0;
          //  log.info("FLUSHHHED");
        }
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
        try {
            RequestTemplateWrapper hostRequest = (RequestTemplateWrapper) o;
            if (request.getRequest().getServerPort() != hostRequest.getRequest().getServerPort() ||
                    request.getRequest().getClientPort() != hostRequest.getRequest().getClientPort())
                return false; // also doing check on client port as if same client can have multiple parallel conns opened to single server
            return request.getRequest().getServerIP().equals(hostRequest.getRequest().getServerIP());
        } catch (ClassCastException exp) {
            AgentToHost host = (AgentToHost) o;
            if (request.getRequest().getServerPort() != host.request.getRequest().getServerPort() ||
                    request.getRequest().getClientPort() != host.request.getRequest().getClientPort())
                return false; // also doing check on client port as if same client can have multiple parallel conns opened to single server
            return request.getRequest().getServerIP().equals(host.request.getRequest().getServerIP());
        }

    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public void hostPacket(ByteBuf packet) {
        if (currentChannelNo == request.getRequest().getNumParallelSockets()) currentChannelNo = 0;
        writeToAgentChannel(channels.get(currentChannelNo), packet);
        currentChannelNo++;
    }

    private void writeToAgentChannel(Channel currentChannel, ByteBuf data) {
        ChannelFuture cf = currentChannel.write(data);
        wCount++;
        if (wCount >= request.getRequest().getBufferSize() * request.getRequest().getNumParallelSockets()) {
            for (Channel channel : channels)
                channel.flush();
            wCount = 0;
        }
    }
    public void transferCompleted() {
        hostStatusInitiator.hostStatusChanged(HostStatusListener.HostStatus.DONE);
    }

    public HostClient getHostClient() {
        return hostClient;
    }
}