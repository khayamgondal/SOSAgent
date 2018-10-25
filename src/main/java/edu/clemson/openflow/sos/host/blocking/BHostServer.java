package edu.clemson.openflow.sos.host.blocking;

import edu.clemson.openflow.sos.manager.ISocketServer;

public class BHostServer implements ISocketServer {
    @Override
    public boolean start() {
        return false;
    }

    @Override
    public boolean stop() {
        return false;
    }
}
