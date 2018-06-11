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


    private int count = 0;
    private HostTrafficShaping shaper;


    public ShapingTimer(HostTrafficShaping shaper) {
        this.shaper = shaper;
    }

    @Override
    public void run() {

        log.info("Called {}", count);
        if (count == 0) shaper.configure(0, TWO_GB);
        if (count == 1) shaper.configure(0, FOUR_GB);
        if (count == 2) shaper.configure(0, SIX_GB);
        if (count == 3) shaper.configure(0, EIGHT_GB);

        else shaper.configure(0, 0);
        count ++;

    }
}
