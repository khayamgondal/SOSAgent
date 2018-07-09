package edu.clemson.openflow.sos.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MappingParser {

    private String clientAgentIP;
    private String serverAgentIP;
    private String serverIP;
    private int serverPort;


    public MappingParser(@JsonProperty("src-agent") String clientAgentIP, @JsonProperty("dst-agent") String serverAgentIP,
                         @JsonProperty("dst-server") String serverIP, @JsonProperty("dst-server-port") int serverPort) {
        this.clientAgentIP = clientAgentIP;
        this.serverAgentIP = serverAgentIP;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public String getClientAgentIP() {
        return clientAgentIP;
    }

    public void setClientAgentIP(String clientAgentIP) {
        this.clientAgentIP = clientAgentIP;
    }

    public String getServerAgentIP() {
        return serverAgentIP;
    }

    public void setServerAgentIP(String serverAgentIP) {
        this.serverAgentIP = serverAgentIP;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
