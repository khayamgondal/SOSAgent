package edu.clemson.openflow.sos.stats;

/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 * This class collects stats like active SOS connections, total connected hosts.
 */
public class StatCollector {

    private int connectedHosts;
    private int totalOpenedConnections;
    private static StatCollector statCollector = new StatCollector();

    public void hostAdded() {
        connectedHosts ++;
    }
    public void hostRemoved() {
        connectedHosts --;
    }
    public void connectionAdded() {
        totalOpenedConnections ++;
    }
    public void connectionRemoved() {
        totalOpenedConnections --;
    }
    public static StatCollector getStatCollector() {
        return statCollector;
    }

    public int getConnectedHosts() {
        return connectedHosts;
    }

    public int getTotalOpenedConnections() {
        return totalOpenedConnections;
    }
}
