package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.RequestParser;

import java.util.EventListener;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This event listener will be called when a new host connects/disconnects
 */
public interface HostStatusListener extends EventListener {

    void hostConnected(RequestParser request, HostStatusInitiater callBackhostStatusInitiater);
    void packetArrived(Object msg);
    void hostDisconnected(String hostIP, int hostPort);
}
