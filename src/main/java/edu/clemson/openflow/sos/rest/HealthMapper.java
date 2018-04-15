package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthMapper {
    private String hostName;
    private double processCPULoad;
    private double systemCPULoad;
    private long freePhyMemSize;
    private long totalPhyMemSize;
    private long committedVirtualMemSize;
    private int connectedHosts;
    private int totalOpenConnections;

    public HealthMapper(@JsonProperty("hostname")String hostName,
                        @JsonProperty("process-cpu-load") double processCPULoad,
                        @JsonProperty("system-cpu-load") double systemCPULoad,
                        @JsonProperty("free-memory-size") long freePhyMemSize,
                        @JsonProperty("total-memory-size")long totalPhyMemSize,
                        @JsonProperty("committed-virtual-memorysize") long committedVirtualMemSize,
                        @JsonProperty("connected-hosts") int connectedHosts,
                        @JsonProperty("total-opened-connections") int totalOpenConnections) {
        this.hostName = hostName;
        this.processCPULoad = processCPULoad;
        this.systemCPULoad = systemCPULoad;
        this.freePhyMemSize = freePhyMemSize;
        this.totalPhyMemSize = totalPhyMemSize;
        this.committedVirtualMemSize = committedVirtualMemSize;
        this.connectedHosts = connectedHosts;
        this.totalOpenConnections = totalOpenConnections;
    }

    public String getHostName() {
        return hostName;
    }

    public double getProcessCPULoad() {
        return processCPULoad;
    }

    public double getSystemCPULoad() {
        return systemCPULoad;
    }

    public long getFreePhyMemSize() {
        return freePhyMemSize;
    }

    public long getTotalPhyMemSize() {
        return totalPhyMemSize;
    }

    public long getCommittedVirtualMemSize() {
        return committedVirtualMemSize;
    }

    public int getConnectedHosts() {  return connectedHosts;    }

    public int getTotalOpenConnections() {  return totalOpenConnections;  }
}
