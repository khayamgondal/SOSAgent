package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AgentPortMapper {

    private ControllerRequestMapper request;
    private List<Integer> ports;

    public AgentPortMapper(@JsonProperty("request") ControllerRequestMapper request,
                           @JsonProperty("ports") List<Integer> ports) {
        this.request = request;
        this.ports = ports;
    }

    public ControllerRequestMapper getRequest() {
        return request;
    }

    public List<Integer> getPorts() {
        return ports;
    }
}
