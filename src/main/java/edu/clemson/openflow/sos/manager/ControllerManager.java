package edu.clemson.openflow.sos.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Khayam Gondal kanjam@g.clemson.edu
 * This class will send the controller connection termination message.
 *     https://github.com/khayamgondal/sos-agent/blob/master/discovery.c
 */
public class ControllerManager {
    private static final Logger log = LoggerFactory.getLogger(ControllerManager.class);

    private String transferID;
    private String controllerIP;
    private static final int DISC_PORT = 9999;
    public ControllerManager(String transferID, String controllerIP) {
        this.controllerIP = controllerIP;
        this.transferID = transferID;
    }

    private String terminationMsg() throws JsonProcessingException {
        TerminationMessageTemplate msg = new TerminationMessageTemplate(transferID);
        ObjectMapper mapperObj = new ObjectMapper();
        return mapperObj.writeValueAsString(msg);
    }

    public boolean sendTerminationMsg() {
        log.info("Sending termination message to controller {}", controllerIP);
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
