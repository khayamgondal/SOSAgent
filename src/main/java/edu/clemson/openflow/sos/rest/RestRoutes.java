package edu.clemson.openflow.sos.rest;

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
    protected static final String TRAFFIC_PATH = "/traffic";


    public RestRoutes(Context context) {
        this.context = context;
    }

    public Restlet getRoutes() {
        Router router = new Router(context);
        router.attach(PathBuilder(HEALTH_PATH), HealthStatus.class);
        router.attach(PathBuilder(TRAFFIC_PATH), TrafficHandler.class);
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
