package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.agent.IncomingRequestListener;
import edu.clemson.openflow.sos.agent.netty.AgentServer;
import edu.clemson.openflow.sos.buf.Demultiplexer;
import edu.clemson.openflow.sos.buf.PacketBuffer;
import edu.clemson.openflow.sos.manager.IncomingRequestManager;
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
import java.util.List;

public class IncomingRequestHandler extends ServerResource{
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(IncomingRequestHandler.class);
    private List<Demultiplexer> demultiplexers;

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            JSONObject request = new JsonRepresentation(entity).getJsonObject();
            IncomingRequestMapper incomingRequest = mapper.readValue(request.toString(), IncomingRequestMapper.class);
            log.info("New ports info from client- agent {}.", incomingRequest.getRequest().getClientAgentIP());
            log.debug("Request Object {}", request.toString());

            IncomingRequestManager incomingRequestManager = IncomingRequestManager.INSTANCE;
            incomingRequestManager.addToPool(incomingRequest);
            log.debug("Added {} to the Ports Pool", incomingRequest.toString()); // need to override tostring yet

            // Also notify the listeners about this new request
            IncomingRequestListener incomingRequestListener = new AgentServer();
            PacketBuffer packetBuffer = new PacketBuffer(incomingRequest);
            log.debug("Buffer assigned for this client request");

            incomingRequestListener.newIncomingRequest(incomingRequest, packetBuffer); //notify the packet receiver about new incoming connection && and also assign a buffer to it.
            log.debug("Notified the server about request & buffer");


            Representation response = new StringRepresentation("TRUE");
            setStatus(Status.SUCCESS_ACCEPTED);
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
