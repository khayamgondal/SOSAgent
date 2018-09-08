package edu.clemson.openflow.sos.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.rest.RequestTemplateWrapper;
import edu.clemson.openflow.sos.rest.RestRoutes;
import edu.clemson.openflow.sos.rest.TerminationMessageTemplate;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This class will send the controller connection termination message.
 *     https://github.com/khayamgondal/sos-agent/blob/master/discovery.c
 */
public class ControllerManager {
    private static final Logger log = LoggerFactory.getLogger(ControllerManager.class);
    private static final int CONTROLLER_REST_PORT = 8001;

    private String transferID;
    private String controllerIP;
    private static final int DISC_PORT = 9999;
    public ControllerManager(String transferID, String controllerIP) {
        this.controllerIP = controllerIP;
        this.transferID = transferID;
    }

    private String buildTerminationMsg() throws JsonProcessingException {
        TerminationMessageTemplate msg = new TerminationMessageTemplate(transferID);
        ObjectMapper mapperObj = new ObjectMapper();
        return mapperObj.writeValueAsString(msg);
    }

    public boolean sendTerminationMsg() {
        log.info("Sending termination message to controller {}", controllerIP);

        String uri = RestRoutes.URIBuilder(controllerIP, CONTROLLER_REST_PORT, "/connection/json");

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost(uri);

        try {
            TerminationMessageTemplate terminatonTemp = new TerminationMessageTemplate(buildTerminationMsg());

        ObjectMapper mapperObj = new ObjectMapper();
        String terminationString = mapperObj.writeValueAsString(terminatonTemp);

        org.apache.http.entity.StringEntity stringEntry = new org.apache.http.entity.StringEntity(terminationString, "UTF-8");
        httpRequest.setEntity(stringEntry);
        HttpResponse response = httpClient.execute(httpRequest);

        log.debug("Sending HTTP request to remote agent {} ", terminationString);
        log.debug("Agent returned HTTP STATUS {} Response {}", response.getStatusLine().getStatusCode(), response.toString());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
