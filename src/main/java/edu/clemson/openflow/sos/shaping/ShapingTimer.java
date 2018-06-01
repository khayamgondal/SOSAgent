package edu.clemson.openflow.sos.shaping;


import java.util.TimerTask;

public class ShapingTimer extends TimerTask {

    private HostTrafficShaping shaper;

    public ShapingTimer(HostTrafficShaping shaper) {
        this.shaper = shaper;
    }

    @Override
    public void run() {

    }
}
