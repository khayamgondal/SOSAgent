package edu.clemson.openflow.sos.host.netty;

import io.netty.channel.ChannelHandlerContext;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by @geddings on 12/19/17.
 */
class HostServerTest {
    private HostServer hostServer;

    @BeforeEach
    void setUp() {
        hostServer = new HostServer();
    }

    @Test
    void testHostServerStartsSocket() {
        assertTrue(hostServer.start());
    }
    
    @Test
    void testHostServerChannelRead() {
        hostServer.channelRead(EasyMock.createNiceMock(ChannelHandlerContext.class), "Message");
    }

}
