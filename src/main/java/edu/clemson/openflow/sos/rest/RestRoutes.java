package edu.clemson.openflow.sos.rest;

import edu.clemson.openflow.sos.host.HostServer;
import edu.clemson.openflow.sos.utils.Utils;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * @author Khayam Anjam kanjam@g.clemson.edu
 **/

public class RestRoutes {
    private Context context;
    protected static final String HTTP_PRESTRING = "http://";
    protected static final String BASE_PATH = "/sos";
    protected static final String API_VERSION = "/v1.0";

    protected static final String HEALTH_PATH = "/health";
    protected static final String REQUEST_PATH = "/request";
    protected static final String PORTMAP_PATH = "/portmap";


    public RestRoutes(Context context) {
        this.context = context;
    }

    public Restlet getRoutes() {
        Router router = new Router(context);
        Utils.router = router;

        router.attach(PathBuilder(HEALTH_PATH), HealthStatus.class);
       // TrafficHandler trafficHandler = new TrafficHandler();
       // TrafficHandler.InternalClass it = trafficHandler.new InternalClass();
       // router.attach(PathBuilder(TRAFFIC_PATH), trafficHandler) ;


        router.attach(PathBuilder(REQUEST_PATH), RequestHandler.class);
        router.attach(PathBuilder(PORTMAP_PATH), RequestHandler.class);

        return router;
    }

    private static String PathBuilder(String path) {
        return BASE_PATH + API_VERSION + path;
    }

    public static String URIBuilder( String IP, String port, String path) {
        return HTTP_PRESTRING + IP + ":" + port + BASE_PATH + API_VERSION + path;
    }

}
