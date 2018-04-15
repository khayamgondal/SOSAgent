package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import edu.clemson.openflow.sos.host.HostClient;
import edu.clemson.openflow.sos.stats.StatCollector;
import edu.clemson.openflow.sos.utils.PrefsSetup;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

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

    private String getComputerName()
    {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else
            return "Unknown Computer";
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Error retrieving hostname");
            e.printStackTrace();
            return null;
        }
    }

    private int getTotalHosts() {
        return StatCollector.getStatCollector().getConnectedHosts();
    }

    private int getOpenedConnections() {
        return StatCollector.getStatCollector().getTotalOpenedConnections();
    }

    private HealthMapper getSystemStats() {
            OperatingSystemMXBean operatingSystemMXBean =
                    (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        HealthMapper healthStatus = new HealthMapper(getHostName(),
                operatingSystemMXBean.getProcessCpuLoad(),
                operatingSystemMXBean.getSystemCpuLoad(),
                operatingSystemMXBean.getFreePhysicalMemorySize(),
                operatingSystemMXBean.getTotalPhysicalMemorySize(),
                operatingSystemMXBean.getCommittedVirtualMemorySize(),
                getTotalHosts(),
                getOpenedConnections());
            return healthStatus;
    }
}
