package edu.clemson.openflow.sos.utils;

import edu.clemson.openflow.sos.rest.RequestListener;
import edu.clemson.openflow.sos.rest.RestServer;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Utils {

    public static List<RequestListener> requestListeners = new ArrayList<>();
    public static Router router;
    public static Properties configFile;
}
