package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.utils.EventListenersLists;
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

public class RequestHandler extends ServerResource{
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);


    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            JSONObject request = new JsonRepresentation(entity).getJsonObject();
            RequestTemplateWrapper incomingRequest = mapper.readValue(request.toString(), RequestTemplateWrapper.class);
            if (incomingRequest.getPorts() != null)log.debug("New ports info from client- agent {}.", incomingRequest.getRequest().getClientAgentIP());
            log.debug("Request Object {}", request.toString());
            // also implement the getting of controller IP
            for (RequestListener requestListener : EventListenersLists.requestListeners) {
                if (requestListener != null) {
                    requestListener.newIncomingRequest(incomingRequest); //notify the packet receiver about new incoming connection && and also assign a buffer to it.
                    log.debug("Notified the agent server handler about request ");
                } else log.warn("Event listener is null.. wont be notifying server about new connection");
            }

            Representation response = new StringRepresentation("TRUE");
            setStatus(Status.SUCCESS_OK);
            return response;

        }catch (IOException e) {
                log.error("Failed to Parse Incoming JSON ports data.");
                e.printStackTrace();
                Representation response = new StringRepresentation("Request data is not valid.");
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return response;
            } catch (NullPointerException e) {
                log.error("Failed to Receive HTTP request for port info.");
                e.printStackTrace();
                Representation response = new StringRepresentation("Not a valid HTTP Request");
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return response;

            }
    }
}
