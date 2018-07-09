package edu.clemson.openflow.sos.utils;

import edu.clemson.openflow.sos.rest.RequestTemplate;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;

public class MockRequestBuilder {


    public RequestTemplateWrapper buildRequest(String clientIP, int clientPort, String clientAgentIP,
                                               String serverAgentIP, int parallelConns, int bufSize,
                                               String serverIP,
                                               int serverPort) {
        RequestTemplate requestTemplate = new RequestTemplate(true, "xxx",
                clientIP, clientPort, serverAgentIP, clientAgentIP,
                parallelConns, bufSize, 5, serverIP, serverPort, true, "0.0.0.0");
        return new RequestTemplateWrapper(requestTemplate, null);

    }
}
