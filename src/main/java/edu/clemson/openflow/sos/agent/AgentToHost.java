package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.buf.Buffer;
import edu.clemson.openflow.sos.buf.OrderedPacketListener;
import edu.clemson.openflow.sos.host.HostClient;
import edu.clemson.openflow.sos.host.HostStatusInitiator;
import edu.clemson.openflow.sos.host.HostStatusListener;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
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
import java.util.Objects;

public class AgentToHost implements OrderedPacketListener, HostPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AgentToHost.class);
    private final HostStatusInitiator hostStatusInitiator;

    private RequestTemplateWrapper request;
    private ArrayList<Channel> channels;
    private HostClient hostClient;
    private Buffer buffer;

    private int currentChannelNo = 0;
    private long totalBytes, startTime, endTime;
    private int wCount;

    int shouldSend = 0;

    /////////////

    SocketChannel sschannel;
    ByteBuffer ssbuffer = ByteBuffer.allocate(65400);
    String s = "f";

////////////////////


    private void normalSocketSend() {
        try {
            if (sschannel.finishConnect()) {
                try {
                    sschannel.write(ssbuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else log.error("NOT CONNECTED :YET");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public AgentToHost(RequestTemplateWrapper request) {
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
        startTime = System.currentTimeMillis();

        javaBufSetup();

    }


    private void javaBufSetup() {
        {
            try {
                sschannel = SocketChannel.open();
                sschannel.configureBlocking(false);
                sschannel.connect(new InetSocketAddress("10.0.0.211", 5001));
                log.info("TRYIGN TO CONNE");
                byte[] dd = s.getBytes();
                for (int i=0; i < ssbuffer.capacity(); i++)
                    ssbuffer.put(i, dd[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean orderedPacket(ByteBuf packet) {
        //normalSocketSend();
        smartSend(packet);
        return true;
        /*
        //TODO: lookinto read/write index
        if (hostClient.getHostChannel().isWritable()) {
            hostClient.getHostChannel().writeAndFlush(packet.slice(4, packet.capacity() - 4));
            wCount++; // will not work if multiple clients connected
            return true;
        }
        else return false;
        */
    }

    private void smartSend(ByteBuf packet) {
        if (shouldSend > 100) {
            hostClient.getHostChannel().write(packet);
            hostClient.getHostChannel().flush();
            shouldSend = 0;
        }
        else {
            if (hostClient.getHostChannel().isWritable()) hostClient.getHostChannel().write(packet);
            shouldSend++;
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
                    request.getRequest().getClientPort() != hostRequest.getRequest().getClientPort()) return false; // also doing check on client port as if same client can have multiple parallel conns opened to single server
            return request.getRequest().getServerIP().equals(hostRequest.getRequest().getServerIP());
        }
        catch (ClassCastException exp) {
            AgentToHost host = (AgentToHost) o;
            if (request.getRequest().getServerPort() != host.request.getRequest().getServerPort() ||
                    request.getRequest().getClientPort() != host.request.getRequest().getClientPort()) return false; // also doing check on client port as if same client can have multiple parallel conns opened to single server
            return request.getRequest().getServerIP().equals(host.request.getRequest().getServerIP());
        }

    }

   /* @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentToHost that = (AgentToHost) o;
        return Objects.equals(hostStatusInitiator, that.hostStatusInitiator) &&
                Objects.equals(request, that.request) &&
                Objects.equals(channels, that.channels) &&
                Objects.equals(hostClient, that.hostClient) &&
                Objects.equals(buffer, that.buffer);
    }*/

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public void hostPacket(ByteBuf packet) {
        if (currentChannelNo == channels.size()) currentChannelNo = 0;
        log.debug("Forwarding packet with size {} on channel {} to Agent-Client", packet.capacity(), currentChannelNo);
     //   log.info(packet.length + "");
        ChannelFuture cf =  channels.get(currentChannelNo).writeAndFlush(packet);
        currentChannelNo ++ ;
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                ReferenceCountUtil.release(packet);
            }
        });
    }

    public void transferCompleted() {
        hostStatusInitiator.hostStatusChanged(HostStatusListener.HostStatus.DONE);
    /*    endTime = System.currentTimeMillis();
        long diffInSec = (endTime - startTime) / 1000;
        System.out.println("KHAYAM Total bytes "+ totalBytes);
        System.out.println("Total time "+ diffInSec);
        System.out.println("Throughput Mbps "+  (totalBytes / diffInSec) * 8 / 1000000 );*/
    }

    public HostClient getHostClient() {
        return hostClient;
    }
}