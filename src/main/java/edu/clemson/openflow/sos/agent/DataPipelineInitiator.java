package edu.clemson.openflow.sos.agent;

import edu.clemson.openflow.sos.rest.ControllerRequestMapper;

import java.util.ArrayList;
import java.util.List;

public class DataPipelineInitiator {
    private List<DataPipelineListener> listeners = new ArrayList<DataPipelineListener>();

    public void addListener(DataPipelineListener toAdd) {
        listeners.add(toAdd);
    }

    public void hostConnected(ControllerRequestMapper request, Object callBackObject) {
        for (DataPipelineListener listener : listeners)
            listener.hostConnected(request, callBackObject);
    }

    public void packetArrived(Object msg) {
        for (DataPipelineListener listener : listeners)
            listener.packetArrived(msg);
    }

}
