package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.manager.SocketManager;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/***
 * @author Khayam Anjam kanjam@g.clemson.edu
 * This class will receive the requests from controller and will initiate a socket connection for each of the client.
 */
public class ControllerRequestHandler extends ServerResource {
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ControllerRequestHandler.class);

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            JSONObject request = new JsonRepresentation(entity).getJsonObject();
            ControllerRequestMapper requestObj = mapper.readValue(request.toString(), ControllerRequestMapper.class);
            log.info("New connection request from controller.");
            log.debug("Request Object {}", request.toString());

            SocketManager socketManager = new SocketManager();
            boolean status = socketManager.socketRequest(requestObj);

            Representation response = new StringRepresentation(Boolean.toString(status));
            setStatus(Status.SUCCESS_ACCEPTED);
            return response;

        } catch (IOException e) {
            log.error("Failed to Parse Incoming JSON data.");
            e.printStackTrace();
            Representation response = new StringRepresentation("Request data is not valid");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return response;
        } catch (NullPointerException e) {
            log.error("Failed to Receive HTTP request.");
            e.printStackTrace();
            Representation response = new StringRepresentation("Not a valid HTTP Request");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return response;

        }

    }
}
