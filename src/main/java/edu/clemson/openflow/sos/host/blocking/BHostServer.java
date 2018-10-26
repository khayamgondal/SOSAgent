package edu.clemson.openflow.sos.host.blocking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.rest.RequestListenerInitiator;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.shaping.RestStatListener;
import edu.clemson.openflow.sos.shaping.ShapingTimer;
import edu.clemson.openflow.sos.utils.MappingParser;
import edu.clemson.openflow.sos.utils.MockRequestBuilder;
import edu.clemson.openflow.sos.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BHostServer implements ISocketServer, RestStatListener, RequestListener {
    private static final Logger log = LoggerFactory.getLogger(BHostServer.class);
    private List<RequestTemplateWrapper> incomingRequests = new ArrayList<>();
    private RequestListenerInitiator requestListenerInitiator;

    private boolean mockRequest;
    private int mockParallelConns;
    private List<MappingParser> mockMapping;
    ObjectMapper mapper = new ObjectMapper();
    private ShapingTimer rateLimitTimer;
    private int hostCheckRate;

    public BHostServer() {

        requestListenerInitiator = new RequestListenerInitiator();
        requestListenerInitiator.addRequestListener(this);

        if (Utils.router != null) {
            Utils.router.getContext().getAttributes().put("host-callback", requestListenerInitiator);
            Utils.router.getContext().getAttributes().put("callback", this); //also pass the callback listener via router context
        }
        if (Utils.configFile != null) {
            mockRequest = Boolean.parseBoolean(Utils.configFile.getProperty("test_mode"));
            mockParallelConns = Integer.parseInt(Utils.configFile.getProperty("test_conns").replaceAll("[\\D]", ""));
        }
        try {
            mockMapping = mapper.readValue(Utils.configFile.getProperty("test_mapping"), new TypeReference<List<MappingParser>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int myMockIndex(String localAgentIP) {
        for (int i = 0; i < mockMapping.size(); i++)
            if (mockMapping.get(i).getClientAgentIP().equals(localAgentIP)) return i;
        return -1;
    }

    private boolean startSocket(int port) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
                Socket s = null;
                try {
                    s = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Thread t = new BHostServerHandler(s);
                t.start();
            }
    }

    @Override
    public boolean start(int port) {
        return startSocket(port);
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public void newIncomingRequest(RequestTemplateWrapper request) {
        incomingRequests.add(request);

    }

    @Override
    public void RestStats(double totalReadThroughput, double totalWriteThroughput) {

    }
}
