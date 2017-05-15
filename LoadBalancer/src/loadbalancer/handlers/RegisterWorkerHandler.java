package loadbalancer.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import loadbalancer.workers.WorkerManager;
import loadbalancer.workers.WorkerWrapper;
import requests.parser.QueryParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by lads on 14/05/2017.
 */
public class RegisterWorkerHandler implements HttpHandler {
    private static Logger logger = Logger.getLogger(LoadBalancerHandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map<String, String> queries = QueryParser.toMap(httpExchange.getRequestURI().getQuery());
        WorkerWrapper worker = new WorkerWrapper(queries.get("a"), queries.get("id"));
        WorkerManager.getInstance().addWorker(worker);
        logger.info("added worker with address: " + worker.getAddress());
        String response = "OK!";
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
