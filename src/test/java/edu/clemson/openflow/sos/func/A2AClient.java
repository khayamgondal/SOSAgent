package edu.clemson.openflow.sos.func;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.rest.RequestMapper;
import edu.clemson.openflow.sos.rest.RequestTemplate;
import edu.clemson.openflow.sos.rest.RestRoutes;
import edu.clemson.openflow.sos.rest.RestServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;

public class A2AClient {

    private static final Logger log = LoggerFactory.getLogger(A2AClient.class);

    private RequestMapper mockRequestBuilder() {
        RequestTemplate requestTemplate = new RequestTemplate(true, "xxx",
                "10.0.0.11", 12000, "10.0.0.21", "10.0.0.11",
                8, 900000, 5, "10.0.0.21", 5001, true);

        RequestMapper mapper = new RequestMapper(requestTemplate, null);
        return mapper;
    }

    private Socket setupClientSocket(String IP, int port) throws IOException {
        return new Socket(IP, port);
    }

    private void restCallToAgentClient(RequestMapper request) throws IOException {

        String uri = RestRoutes.URIBuilder(request.getRequest().getClientAgentIP(), "8002", "/request");
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost(uri);

        ObjectMapper mapperObj = new ObjectMapper();
        String requestString = mapperObj.writeValueAsString(request);
        org.apache.http.entity.StringEntity stringEntry = new org.apache.http.entity.StringEntity(requestString, "UTF-8");
        httpRequest.setEntity(stringEntry);
     //   log.debug("JSON Object to sent {}", requestString);

        HttpResponse response = httpClient.execute(httpRequest);

        log.info("Sending HTTP request to client side agent with port info {}", request.getRequest().getServerAgentIP());
        log.info("Agent returned {}", response.getStatusLine().getStatusCode());
    }


    private String createPacketOfBytes(int bytes) {
        StringBuilder sb = new StringBuilder(bytes);
        for (int i=0; i<bytes; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
    @Test
    public void main() {

        RestServer restServer = new RestServer();
        assertEquals(restServer.startComponent(), true);

        HostServer hostServer = new HostServer();
        assertEquals(hostServer.start(), true);

      //  AgentServer agentServer = new AgentServer();
      //  assertEquals(agentServer.start(), true);

        RequestMapper request = mockRequestBuilder();


        try {

            restCallToAgentClient(request);

            Socket socket = setupClientSocket(request.getRequest().getClientAgentIP(), 9877);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            String b = createPacketOfBytes(800);
            long startTime = System.currentTimeMillis();
            long totalBytes = 0;
            for (long i=0; i < 99000000; i ++){
                oos.writeObject(b);
                totalBytes += 800;
            }
            long endTime = System.currentTimeMillis();
            long diffInSec = (endTime - startTime) / 1000;
            System.out.println("Total bytes "+ totalBytes);
            System.out.println("Total time "+ diffInSec);
            System.out.println("Throughput Gbps "+  (totalBytes / diffInSec) * 8 / 1000000000 );

            oos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
