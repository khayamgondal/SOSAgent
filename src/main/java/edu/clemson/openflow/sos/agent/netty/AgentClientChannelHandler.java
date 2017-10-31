package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.agent.HostStatusInitiater;
import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.host.netty.HostServer;
import edu.clemson.openflow.sos.host.netty.HostServerChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class AgentClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(AgentClientChannelHandler.class);
    private HostStatusInitiater hostStatusInitiater;
    private HostServerChannelHandler hostServerChannelHandler ;

    //private Channel remoteChannel;
    //private ChannelHandlerContext channelHandlerContext;

    public AgentClientChannelHandler() {


    }
    public AgentClientChannelHandler(Channel remoteChannel) {
      //  this.remoteChannel = remoteChannel;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        hostStatusInitiater = new HostStatusInitiater();
        hostServerChannelHandler = new HostServerChannelHandler();
        hostStatusInitiater.addListener(hostServerChannelHandler);
    }

    /**
     * Read from the server and write back using @param ctx
     **/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress(); // need to remove this. just send msg . send sock on active

        hostStatusInitiater.packetArrived(socketAddress.getHostName(), socketAddress.getPort(), msg); //forward packet to handlers

       // this.channelHandlerContext = ctx;
    //write back the received message to remote channel.
        //remoteChannel.writeAndFlush(msg);
    }


}
