package edu.clemson.openflow.sos.shaping;

import edu.clemson.openflow.sos.rest.TrafficHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapingTimer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ShapingTimer.class);

    private static final long NO_LIMIT = 0;
    private static final long TWO_GB = 200000000;
    private static final long FOUR_GB = 400000000;
    private static final long SIX_GB = 600000000;
    private static final long EIGHT_GB = 800000000;
    private static final long TEN_GB = 800000000;


    private int count = 0;
    private HostTrafficShaping shaper;


    public ShapingTimer(HostTrafficShaping shaper) {
        this.shaper = shaper;
    }

    @Override
    public void run() {
            int nor = count % 7;
        log.info("NOr is {}", nor);
        if (nor == 2) shaper.configure(0, TWO_GB);
        if (nor == 3) shaper.configure(0, FOUR_GB);
        if (nor == 4) shaper.configure(0, SIX_GB);
        if (nor == 5) shaper.configure(0, EIGHT_GB);
        if (nor == 5) shaper.configure(0, TEN_GB);
        if (nor == 6) shaper.configure(0, 0);

        else shaper.configure(0, (long) TrafficHandler.readRate);
        count ++;

    }
}
