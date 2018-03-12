package edu.clemson.openflow.sos.host;

/**
 * @author Khayam Gondal
 * @email kanjam@g.clemson.edu
 * If end host status changes such as user cancels opertion or transfer is done, connectivity issue, we might want to notify related agent == agent communication handlers.
 */

public interface HostStatusListener {

    enum HostStatus {
        NORMAL,
        ERROR,
        DONE
    }

    void HostStatusChanged(HostStatus hostStatus);
}
