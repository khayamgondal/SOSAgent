package edu.clemson.openflow.sos;

import edu.clemson.openflow.sos.agent.AgentServer;
import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.rest.RestServer;
import edu.clemson.openflow.sos.utils.PrefsSetup;

/**
    @author Khayam Anjam    kanjam@g.clemson.edu
    This is the main entry point of the Agents. We will be starting all modules from here
 **/
public class Main {

    public static void main(String[] args) {
      //  PrefsSetup prefsSetup = new PrefsSetup();
      //  prefsSetup.loadDefault(); //load default settings

        RestServer restServer = new RestServer();
        restServer.startComponent();

        HostServer hostServer = new HostServer();
        hostServer.start();

        AgentServer agentServer = new AgentServer();
        agentServer.start();

    }
}
