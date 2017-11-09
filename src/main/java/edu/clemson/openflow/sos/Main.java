package edu.clemson.openflow.sos;

import edu.clemson.openflow.sos.agent.netty.AgentServer;
import edu.clemson.openflow.sos.host.netty.HostServer;
import edu.clemson.openflow.sos.rest.RestServer;
import edu.clemson.openflow.sos.utils.PrefsSetup;

/**
    @author Khayam Anjam    kanjam@g.clemson.edu
    This is the main entry point of the Agents. We will be starting all modules from here
 **/
public class Main {

    public static void main(String[] args) {
        PrefsSetup prefsSetup = new PrefsSetup();
        prefsSetup.loadDefault(); //load default settings

        try { //Start rest server
            RestServer restServer = new RestServer();
            restServer.startComponent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        HostServer hostServer = new HostServer(); //Start hostServer
        hostServer.start();
        AgentServer agentServer = new AgentServer();
        agentServer.start();

    }
}
