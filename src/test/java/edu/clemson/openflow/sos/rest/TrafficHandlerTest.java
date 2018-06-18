package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.shaping.StatsTemplate;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TrafficHandlerTest {
    private static final String TRAFFIC_PATH = "/traffic";
    private static final String REST_PORT = "8002";
    private static final int AGENT_DATA_PORT = 9878;

    @Test
    public void sendRestRequest() {
        String uri = RestRoutes.URIBuilder("127.0.0.1", REST_PORT, TRAFFIC_PATH);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost(uri);

        StatsTemplate statsTemplate = new StatsTemplate(00, 00);
        ObjectMapper mapperObj = new ObjectMapper();
        try {
            String portMapString = mapperObj.writeValueAsString(statsTemplate);
            org.apache.http.entity.StringEntity stringEntry = new org.apache.http.entity.StringEntity(portMapString, "UTF-8");
            httpRequest.setEntity(stringEntry);
            HttpResponse response = httpClient.execute(httpRequest);
            assertTrue(response.getStatusLine().getStatusCode() == 200);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}