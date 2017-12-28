package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestMapper {

    private RequestTemplate request;
    private List<Integer> ports;

    public RequestMapper(@JsonProperty("request") RequestTemplate request,
                         @JsonProperty("ports") List<Integer> ports) {
        this.request = request;
        this.ports = ports;
    }

    public RequestTemplate getRequest() {
        return request;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    @Override
    public String toString() {
        return "RequestMapper{" +
                "request=" + request.toString() +
                ", ports=" + ports +
                '}';
    }
}
