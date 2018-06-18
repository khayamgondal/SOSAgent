package edu.clemson.openflow.sos.shaping;

public interface RestStatListener {

    public void RestStats(double totalReadThroughput, double totalWriteThroughput);
}
