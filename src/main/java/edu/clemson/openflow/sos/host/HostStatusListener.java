package edu.clemson.openflow.sos.host;

/**
 * @author Khayam Gondal
 * @email kanjam@g.clemson.edu
 * If end host status changes such as user cancels operation or transfer is done,
 * connectivity issue, we might want to notify related agent == agent communication handlers so they can
 * terminate their sockets.
 */

public interface HostStatusListener {

    enum HostStatus {
        NORMAL,
        ERROR,
        DONE
    }

    void HostStatusChanged(HostStatus hostStatus);
}
