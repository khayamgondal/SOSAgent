package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.RequestParser;

import java.util.EventListener;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This event listener will be called when a new host connects/disconnects
 */
public interface HostStatusListener extends EventListener {

    void hostConnected(RequestParser request);
    void packetArrived(String hostIP, int hostPort, Object msg);
    void hostDisconnected(String hostIP, int hostPort);
}
