package edu.clemson.openflow.sos.shaping;

public interface IStatListener {

    void notifyStats(long WrittenThroughputBytes);
}
