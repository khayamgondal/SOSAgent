package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.ControllerRequestMapper;

import java.util.EventListener;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This event listener will be called when a new host connects/disconnects
 */
public interface DataPipelineListener extends EventListener {

    void hostConnected(ControllerRequestMapper request, Object callBackObject);
    void packetArrived(Object msg);
    void hostDisconnected(String hostIP, int hostPort);
}
