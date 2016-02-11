package edu.clemson.openflow.sos.shaping;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StatsTemplate {

    private double totalReadThroughput;
    private double totalWriteThroughput;


    public StatsTemplate(@JsonProperty("written") double totalWriteThroughput, @JsonProperty("read") double totalReadThroughput) {
        this.totalReadThroughput = totalReadThroughput;
        this.totalWriteThroughput = totalWriteThroughput;
    }

    public double getTotalReadThroughput() {
        return totalReadThroughput;
    }

    public double getTotalWriteThroughput() {
        return totalWriteThroughput;
    }

    @Override
    public String toString() {
        return "Written "+ totalWriteThroughput + "Read " + totalReadThroughput;
    }
}
