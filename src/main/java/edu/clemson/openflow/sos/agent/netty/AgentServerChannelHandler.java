package edu.clemson.openflow.sos.agent.netty;

import edu.clemson.openflow.sos.host.netty.HostClient;
import edu.clemson.openflow.sos.manager.RequestManager;
import edu.clemson.openflow.sos.rest.RequestParser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class AgentServerChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(AgentServerChannelHandler.class);
    private RequestParser request;
    private Channel remoteChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("New agent-side connection from {} at Port {}",
                socketAddress.getHostName(),
                socketAddress.getPort());

        RequestManager requestManager = RequestManager.INSTANCE;
        request = requestManager.getRequest(socketAddress.getHostName(),
                socketAddress.getPort(), false);
        if (request == null)
            log.warn("No request found for {} .. not gonna forward it",
                    socketAddress.getHostName());
        else {
            //Start up the HostClient which will sent packets to Server.
            HostClient hostClient = new HostClient(request.getServerIP(), request.getServerPort(), ctx.channel()); // we are passing our channel to HostClient so It can write back the response messages
            hostClient.start();
            this.remoteChannel = hostClient.getChannel();
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        if (remoteChannel != null) { // write to the opened channel
            log.debug("received packet from agent {}, destined for server {}", request.getClientAgentIP(), request.getServerIP());
            //byte[] m = (byte[] ) msg;
            //log.info(new String(m));
            remoteChannel.writeAndFlush(msg);

        }
        else {
            log.error("Couldn't connect to server {}", request.getServerIP());
        }
        ReferenceCountUtil.release(msg);

    }
}
