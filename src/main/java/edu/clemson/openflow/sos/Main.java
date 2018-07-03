package edu.clemson.openflow.sos;

import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.manager.ISocketServer;
import edu.clemson.openflow.sos.rest.RestServer;
import edu.clemson.openflow.sos.utils.PrefsSetup;
import edu.clemson.openflow.sos.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
    @author Khayam Anjam    kanjam@g.clemson.edu
    This is the main entry point of the Agents. We will be starting all modules from here
 **/
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
      //  PrefsSetup prefsSetup = new PrefsSetup();
      //  prefsSetup.loadDefault(); //load default settings

        try {
            Properties properties = new Properties();
            String filename = "config.properties";
            InputStream input = Main.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                log.warn("Unable to find default configuration");
            } else {
                properties.load(input);
                Utils.configFile = properties;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        RestServer restServer = new RestServer(); // port 8002
        restServer.startComponent();

        ISocketServer hostServer = new HostServer(); // port 9877
        hostServer.start();

        ISocketServer agentServer = new AgentServer(); // port 9878
        agentServer.start();

    }
}
