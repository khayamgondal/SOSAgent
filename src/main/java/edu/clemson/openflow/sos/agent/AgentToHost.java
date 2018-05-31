package edu.clemson.openflow.sos.agent;

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

import java.util.ArrayList;

public class AgentToHost implements OrderedPacketListener, HostPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AgentToHost.class);
    private final HostStatusInitiator hostStatusInitiator;

    private RequestTemplateWrapper request;
    private ArrayList<Channel> channels;
    private HostClient hostClient;
    private int currentChannelNo = 0;
    private long totalBytes, startTime, endTime;
    private int wCount;

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
    }

    @Override
    public void orderedPacket(ByteBuf packet) {
        log.debug("Got new sorted packet");
    //    log.info("Order count {}", packet.refCnt());
        totalBytes += packet.capacity();
       // byte[] bytes = new byte[packet.capacity() - 4 ]; //SLOW
       // packet.getBytes(4, bytes);
    //    ChannelFuture cf = hostClient.getHostChannel().write(bytes);
        //TODO: lookinto read/write index
       /* ChannelFuture cf = */
       // hostClient.getHostChannel().writeAndFlush(packet.slice(4, packet.capacity() - 4));
        packet.release();

      /*    cf.addListener(new ChannelFutureListener() {
             @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //ReferenceCountUtil.release(packet);
                 log.info("After flush {}", packet.refCnt());
            }
        });*/

        wCount++; // will not work if multiple clients are connected...maintaince own couter using manager ?
   /*     if (wCount >= request.getRequest().getQueueCapacity()) {

            hostClient.getHostChannel().flush(); //packet.release();
            wCount = 0;
            //log.info("Flushed all channels");
        }*/
     //   hostClient.getHostChannel().writeAndFlush(packet);
     //    packet.release();
        //ReferenceCountUtil.release(packet);
        //hostClient.getHostChannel().flush();                packet.release();


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

        RequestTemplateWrapper host = (RequestTemplateWrapper) o;

        if (request.getRequest().getServerPort() != host.getRequest().getServerPort()) return false;
        return request.getRequest().getServerIP().equals(host.getRequest().getServerIP());
    }

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