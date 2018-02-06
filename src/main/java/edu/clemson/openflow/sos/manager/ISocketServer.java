package edu.clemson.openflow.sos.manager;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 */
public interface ISocketServer {

    /**
    This method should return true if socket server is running. Incase of a error false should be returned.
    This method returns the result to http call from floodlight.
     You can have your own strategies on how to handle the traffic i.e. types of sockets you wanna use.
     **/
    boolean start();
    boolean stop();
}
