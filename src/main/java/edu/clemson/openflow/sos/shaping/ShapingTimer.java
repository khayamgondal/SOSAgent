package edu.clemson.openflow.sos.shaping;

import edu.clemson.openflow.sos.rest.TrafficHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapingTimer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ShapingTimer.class);

    private HostTrafficShaping shaper;

    public ShapingTimer(HostTrafficShaping shaper) {
        this.shaper = shaper;
    }

    @Override
    public void run() {
        shaper.configure(0, (long) TrafficHandler.readRate);
    }
}
