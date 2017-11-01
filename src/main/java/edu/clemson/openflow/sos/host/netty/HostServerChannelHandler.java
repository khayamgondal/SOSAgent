package edu.clemson.openflow.sos.host.netty;

import edu.clemson.openflow.sos.agent.HostStatusInitiater;
import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.agent.netty.AgentClient;
import edu.clemson.openflow.sos.agent.netty.AgentClientChannelHandler;
import edu.clemson.openflow.sos.manager.RequestManager;
import edu.clemson.openflow.sos.rest.RequestParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 *  * TODO: Before connecting to client, compare its IP address with the one we are getting in requestParser Obj

 */
public class HostServerChannelHandler extends ChannelInboundHandlerAdapter implements HostStatusListener {
    private static final Logger log = LoggerFactory.getLogger(HostServerChannelHandler.class);
    private RequestParser request;

    //private Channel remoteChannel; // remote channel to write to
    private HostStatusInitiater hostStatusInitiater, callBackhostStatusInitiater;
    private AgentClient agentClient;
    private Channel myChannel;

    public HostServerChannelHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("New host-side connection from {} at Port {}",
                socketAddress.getHostName(),
                socketAddress.getPort());

        RequestManager requestManager = RequestManager.INSTANCE;
        this.request = requestManager.getRequest(socketAddress.getHostName(),
                socketAddress.getPort(), true);

        myChannel = ctx.channel();


        if (request != null) {
            hostStatusInitiater = new HostStatusInitiater();
            callBackhostStatusInitiater = new HostStatusInitiater();
            agentClient = new AgentClient();
            agentClient.start(request.getServerAgentIP());
            hostStatusInitiater.addListener(agentClient);
            hostStatusInitiater.hostConnected(request, callBackhostStatusInitiater); //also pass the call back handler so It can respond back
        }
        else log.error("Couldn't find the request {} in request pool. Not notifying agent",
                request.toString());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (request != null) {
            hostStatusInitiater.packetArrived(msg); //notify handlers
        }
        else log.error("Couldn't find the request {} in request pool. " +
                "Not forwarding packet", request.toString());
        //if (remoteChannel != null) {
        //    remoteChannel.writeAndFlush(msg);
        //    }
        //    else {
        //    log.error("Couldn't connect to remote agent {}", request.getServerAgentIP());
        //}
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void hostConnected(RequestParser request, HostStatusInitiater hostStatusInitiater) {

    }

    @Override
    public void packetArrived(Object msg) {
        log.debug("Received new packet from agent sending to host");
        if (myChannel == null) log.error("Current context is null, wont be sending packet back to host");
        else myChannel.writeAndFlush(msg);
    }

    @Override
    public void hostDisconnected(String hostIP, int hostPort) {

    }
}
