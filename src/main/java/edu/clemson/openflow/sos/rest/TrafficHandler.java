package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.shaping.RestStatListener;
import edu.clemson.openflow.sos.shaping.StatsTemplate;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.JSONObject;
import org.restlet.Restlet;
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

    private RestStatListener restStatListener;

    public static double readRate; // for now I am using a public static..
    // but need to change this to an event based model. where once rate is updated
    // it notifies the HostServer and that adjusts it rate accordingly ......

    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(TrafficHandler.class);


    @Override
    protected void doInit() throws ResourceException {
        System.out.println("FFFFFFFFFFFFFF"+ getContext().getAttributes().get("test"));
    }

    public void setRestListener(HostServer listener) {
        this.restStatListener = listener;
    }

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        try {
            JSONObject request = new JsonRepresentation(entity).getJsonObject();
            StatsTemplate statsTemplate = mapper.readValue(request.toString(), StatsTemplate.class);
            double dd = statsTemplate.getTotalReadThroughput() * 8 / 1024 / 1024;
            readRate = statsTemplate.getTotalReadThroughput();

            log.info("Read size Mbps {}", dd);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Representation response = new StringRepresentation("TRUE");
        setStatus(Status.SUCCESS_OK);
        return response;
    }

}
