package webserver;

import com.sun.net.httpserver.HttpServer;
import webserver.handlers.ImageHandler;
import webserver.handlers.RenderRequestHandler;
import webserver.handlers.TestHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server implements Runnable {
    private final static String LOAD_BALANCER = "http://localhost:8181/register?";
    private final static Logger logger = Logger.getLogger(Server.class.getName());
    private final static int PORT    = Integer.getInteger("render.port", 8080);
    private final static int THREAD_POOL = 5;
    private final static String RENDER_ROUTE = "/r.html";
    private static Server renderInstance;
    private HttpServer httpServer;
    private ExecutorService executor;

    @Override
    public void run() {
        try {
            executor = Executors.newFixedThreadPool(THREAD_POOL);

            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            logger.info("Creating RenderServer at port: " + PORT);
            httpServer.createContext(RENDER_ROUTE, new RenderRequestHandler());
            logger.info("Setup route: " + RENDER_ROUTE + " with handler " + RenderRequestHandler.class.getName());
            httpServer.createContext("/image", new ImageHandler());
            httpServer.createContext("/test", new TestHandler());
            httpServer.setExecutor(executor);
            logger.info("Setup executor as a fixed thread pool with " + THREAD_POOL + " threads");
            httpServer.start();
            logger.info("Started RenderServer!");

            // Wait here until notified of shutdown.
            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                    logger.warning("There was an exception waiting for shutdown, check the stacktrace for more errors.");
                    logger.warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException t) {
            logger.warning("There was an exception handling, check the stacktrace for more errors.");
            logger.warning(t.getMessage());
            t.printStackTrace();
        }
    }

    static void shutdown() {
        try {
            logger.info("Shutting down the RenderServer!");
            renderInstance.httpServer.stop(0);
        } catch (Exception e) {
            logger.warning("There was an exception when shutting down the server, check the stacktrace");
            logger.warning(e.getMessage());
            e.printStackTrace();
        } finally {
            logger.info("Server shut down!");
        }

        synchronized (renderInstance) {
            renderInstance.notifyAll();
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("Getting machine ip and register at loadbalancer");
        registerWorker();
        logger.info("Start RenderInstance: ");
        renderInstance = new Server();
        Thread serverThread = new Thread(renderInstance);
        serverThread.start();
        Runtime.getRuntime().addShutdownHook(new OnShutdown());
        try {
            serverThread.join();
            logger.info("RenderInstance Ended!");
        } catch (Exception e) {
            logger.warning("RenderInstance could not be stopped properly: ");
            logger.warning(e.getMessage());
        }
    }

    private static void registerWorker() {
        //TODO get machine id and ip
        HttpURLConnection connection = null;
        try {

            String address = "localhost";
            String id = "worker1";
            URL url = null;
            url = new URL(LOAD_BALANCER + "a=" + address + "&id=" + id);
            logger.info("connecting to: " + url.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            logger.info(connection.getResponseMessage());
        } catch (Exception e) {
            logger.warning("Could not register at loadbalancer");
            logger.warning(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}

class OnShutdown extends Thread {
    public void run() {
        Server.shutdown();
    }
}
