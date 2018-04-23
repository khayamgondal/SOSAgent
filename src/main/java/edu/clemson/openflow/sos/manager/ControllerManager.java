package edu.clemson.openflow.sos.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This class will send the controller connection termination message.
 */
public class ControllerManager {
    //https://github.com/khayamgondal/sos-agent/blob/master/discovery.c

    private String transferID;
    private String controllerIP;
    private static final int DISC_PORT = 9998;
    public ControllerManager(String transferID) {
        this.transferID = transferID;
    }

    private String terminationMsg() throws JsonProcessingException {
        TerminationMessageTemplate msg = new TerminationMessageTemplate(transferID);
        ObjectMapper mapperObj = new ObjectMapper();
        return mapperObj.writeValueAsString(msg);
    }

    public boolean sendTerminationMsg() {
        try {
            Socket socket = new Socket(controllerIP, DISC_PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeBytes(terminationMsg());
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
