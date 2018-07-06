package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;

public class RequestHandler extends ServerResource {
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private RequestListenerInitiator hostCallbackInitiators ;
    private RequestListenerInitiator agentCallbackInitiators ;


    @Override
    protected void doInit() throws ResourceException {
        hostCallbackInitiators = (RequestListenerInitiator) getContext().getAttributes().get("host-callback");
        agentCallbackInitiators = (RequestListenerInitiator) getContext().getAttributes().get("agent-callback");

    }

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            JSONObject request = new JsonRepresentation(entity).getJsonObject();
            RequestTemplateWrapper incomingRequest = mapper.readValue(request.toString(), RequestTemplateWrapper.class);
            log.info("Received Request from remote agent {}", incomingRequest);
            if (incomingRequest.getPorts() != null)
                log.debug("New ports info from client- agent {}.", incomingRequest.getRequest().getClientAgentIP());
            String ctlIP = getClientInfo().getAddress();
            incomingRequest.getRequest().setControllerIP(ctlIP);
            log.debug("Request Object {}", request.toString());

            if (incomingRequest.getPorts() == null && hostCallbackInitiators != null) hostCallbackInitiators.newIncomingRequest(incomingRequest);
            else if (incomingRequest.getPorts() != null && agentCallbackInitiators != null) agentCallbackInitiators.newIncomingRequest(incomingRequest);

            Representation response = new StringRepresentation("TRUE");
            setStatus(Status.SUCCESS_OK);
            return response;

        } catch (IOException e) {
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
