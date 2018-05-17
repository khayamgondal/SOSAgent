package edu.clemson.openflow.sos.shaping;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

public class HostTrafficShaping extends GlobalTrafficShapingHandler {

    private static final Logger log = LoggerFactory.getLogger(HostTrafficShaping.class);

    public HostTrafficShaping(ScheduledExecutorService executor, long writeLimit, long readLimit, long checkInterval) {

        super(executor, writeLimit, readLimit, checkInterval);
    }



}
