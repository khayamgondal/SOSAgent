package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import edu.clemson.openflow.sos.utils.PrefsSetup;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 * Class to return the agent status to the controller. Such as How many no. of clients this
 * agent supports, underlying agent2agent technology it supports. Controller need to make decision
 * based on these matrics. socket technology being used i.e java host, netty etc
 * TODO: return no. of active client connections.
 * TODO: Write the post call to be able to config agent i.e. drivers & max supported clients
 */
public class HealthStatus extends ServerResource {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HealthStatus.class);
    ObjectMapper mapper = new ObjectMapper();


    @Override
    protected Representation get() throws ResourceException {
        // PrefsSetup  prefs = new PrefsSetup();
        // String prefsString = prefs.getPrefs();
        // log.debug("Prefs are {}", prefsString);
        try {
            return new JsonRepresentation(mapper.writeValueAsString(getSystemStats()));
        } catch (JsonProcessingException e) {
            log.error("Error converting Object to JSON");
            e.printStackTrace();

        }
        return new JsonRepresentation("Error Reading System stats");
    }

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        return super.post(entity);
    }

    private HealthMapper getSystemStats() {
        HealthMapper healthStatus = null;
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            OperatingSystemMXBean operatingSystemMXBean =
                    (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            healthStatus = new HealthMapper(hostName,
                    operatingSystemMXBean.getProcessCpuLoad(),
                    operatingSystemMXBean.getSystemCpuLoad(),
                    operatingSystemMXBean.getFreePhysicalMemorySize(),
                    operatingSystemMXBean.getTotalPhysicalMemorySize(),
                    operatingSystemMXBean.getCommittedVirtualMemorySize());
            return healthStatus;
        } catch (UnknownHostException e) {
            log.error("Unable to retrieve hostname");
            e.printStackTrace();
        }
        return healthStatus;
    }
}
