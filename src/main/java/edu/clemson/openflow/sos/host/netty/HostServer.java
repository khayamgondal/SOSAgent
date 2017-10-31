package edu.clemson.openflow.sos.host.netty;

import edu.clemson.openflow.sos.agent.HostStatusListener;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.manager.SocketManager;
import edu.clemson.openflow.sos.rest.RequestParser;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 * this class will start a new thread for every incoming connection from clients
 */
public class HostServer implements ISocketServer {
    private static final Logger log = LoggerFactory.getLogger(SocketManager.class);
    private static final int DATA_PORT = 9877;
    private Channel myChannel;

    private boolean startSocket(int port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new HostServerChannelInitializer()
                    );

            ChannelFuture f = b.bind().sync();
            myChannel = f.channel();
            log.info("Started host-side socket server at Port {}", port);
            return true;
            // Need to do socket closing handling. close all the remaining open sockets
            //System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            //f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error starting host-side socket");
            e.printStackTrace();
            return false;
        } finally {
            //group.shutdownGracefully().sync();
        }
    }

    @Override
    public boolean start() {
        return startSocket(DATA_PORT);
    }


}
