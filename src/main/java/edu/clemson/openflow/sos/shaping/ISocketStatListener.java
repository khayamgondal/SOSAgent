package edu.clemson.openflow.sos.shaping;

//TODO: Create a helper class to carry list of all the listeners incase we have multiple listeners. Currently I just have one listeners so I can use setListener
public interface ISocketStatListener {

    void SocketStats(long lastWriteThroughput, long lastReadThroughput);
}
