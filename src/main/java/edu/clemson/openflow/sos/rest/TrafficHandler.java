package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.shaping.StatsTemplate;
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

public class TrafficHandler extends ServerResource {

    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(TrafficHandler.class);


    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            JSONObject request = new JsonRepresentation(entity).getJsonObject();
            StatsTemplate statsTemplate = mapper.readValue(request.toString(), StatsTemplate.class);
            double dd = statsTemplate.getTotalReadThroughput() * 8 /1024 / 1024;
            log.info("Read size Mbps {}", dd);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Representation response = new StringRepresentation("TRUE");
        setStatus(Status.SUCCESS_OK);
        return response;
    }

}
