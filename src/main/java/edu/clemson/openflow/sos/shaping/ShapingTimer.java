package edu.clemson.openflow.sos.shaping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapingTimer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ShapingTimer.class);

    private static final long NO_LIMIT = 0;
    private static final long TWO_GB = 200000000;
    private static final long FOUR_GB = 400000000;
    private static final long SIX_GB = 600000000;
    private static final long EIGHT_GB = 800000000;
    private static final long TEN_GB = 1000000000;

    private HostTrafficShaping shaper;

    private double totalReadThroughput;

    public ShapingTimer(HostTrafficShaping shaper) {
        this.shaper = shaper;
    }

    public void setTotalReadThroughput(double totalReadThroughput) {
        this.totalReadThroughput = totalReadThroughput;
    }

    @Override
    public void run() {
        log.info("Limiting rate to {} Gbps", totalReadThroughput * 8 / 1024 / 1024 / 1024);
        //shaper.configure(0, (long) totalReadThroughput);

    }
}
