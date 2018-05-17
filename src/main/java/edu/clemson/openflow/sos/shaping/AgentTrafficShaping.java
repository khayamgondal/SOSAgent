package edu.clemson.openflow.sos.shaping;

import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class AgentTrafficShaping extends GlobalChannelTrafficShapingHandler  {
    private static final Logger log = LoggerFactory.getLogger(AgentTrafficShaping.class);

    private final List<Long> cumulativeWrittenBytes = new LinkedList<Long>();
    private final List<Long> cumulativeReadBytes = new LinkedList<Long>();
    private final List<Long> throughputWrittenBytes = new LinkedList<Long>();
    private final List<Long> throughputReadBytes = new LinkedList<Long>();


    public AgentTrafficShaping(ScheduledExecutorService executor, long checkInterval) {
        super(executor, checkInterval);
    }

    public AgentTrafficShaping(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit,
                               long writeChannelLimit, long readChannelLimit, long checkInterval) {
        super(executor, writeGlobalLimit, readGlobalLimit, writeChannelLimit, readChannelLimit, checkInterval);
    }

    /**
     * Override to compute average of bandwidth between all channels
     */
    @Override
    protected void doAccounting(TrafficCounter counter) {
        long maxWrittenNonZero = this.maximumCumulativeWrittenBytes();
        if (maxWrittenNonZero == 0) {
            maxWrittenNonZero = 1;
        }
        long maxReadNonZero = this.maximumCumulativeReadBytes();
        if (maxReadNonZero == 0) {
            maxReadNonZero = 1;
        }
      //  log.info("Counterrrrrrr {}", this.channelTrafficCounters().size());
        for (TrafficCounter tc : this.channelTrafficCounters()) {
            long cumulativeWritten = tc.cumulativeWrittenBytes();
            if (cumulativeWritten > maxWrittenNonZero) {
                cumulativeWritten = maxWrittenNonZero;
            }
            cumulativeWrittenBytes.add((maxWrittenNonZero - cumulativeWritten) * 100 / maxWrittenNonZero);
            throughputWrittenBytes.add(tc.getRealWriteThroughput() >> 10);
            long cumulativeRead = tc.cumulativeReadBytes();
            if (cumulativeRead > maxReadNonZero) {
                cumulativeRead = maxReadNonZero;
            }
            cumulativeReadBytes.add((maxReadNonZero - cumulativeRead) * 100 / maxReadNonZero);
            throughputReadBytes.add(tc.lastReadThroughput() >> 10);
        }
        log.info(this.toString() + " QueuesSize: " + queuesSize()
                + "\nWrittenBytesPercentage: " + cumulativeWrittenBytes
                + "\nWrittenThroughputBytes: " + throughputWrittenBytes
                + "\nReadBytesPercentage:    " + cumulativeReadBytes
                + "\nReadThroughputBytes:    " + throughputReadBytes);
        cumulativeWrittenBytes.clear();
        cumulativeReadBytes.clear();
        throughputWrittenBytes.clear();
        throughputReadBytes.clear();
        super.doAccounting(counter);
    }
}
