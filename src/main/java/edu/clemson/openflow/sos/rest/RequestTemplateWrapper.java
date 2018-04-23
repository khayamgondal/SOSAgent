package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestTemplateWrapper {

    private RequestTemplate request;
    private List<Integer> ports;

    public RequestTemplateWrapper(@JsonProperty("request") RequestTemplate request,
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
        return "RequestTemplateWrapper{" +
                "request=" + request.toString() +
                ", ports=" + ports +
                '}';
    }
}
