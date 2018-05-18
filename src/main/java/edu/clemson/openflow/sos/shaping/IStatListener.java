package edu.clemson.openflow.sos.shaping;

import java.util.List;

public interface IStatListener {

    void notifyStats(List<Long> writtenThroughputBytes);
}
