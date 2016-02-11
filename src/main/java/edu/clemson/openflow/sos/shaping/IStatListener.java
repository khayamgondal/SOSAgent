package edu.clemson.openflow.sos.shaping;

import java.util.List;

public interface IStatListener {

    void notifyStats(long lastWriteThroughput, long lastReadThroughput);
}
