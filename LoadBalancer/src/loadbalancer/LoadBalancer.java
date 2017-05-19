package loadbalancer;

import com.sun.net.httpserver.HttpServer;
import loadbalancer.handlers.*;
import loadbalancer.workers.WorkerManager;
import loadbalancer.workers.WorkerWrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LoadBalancer implements Runnable{

    private final static Logger logger = Logger.getLogger(LoadBalancer.class.getName());
    private final static int PORT    = Integer.getInteger("balancer.port", 8181);
    private final static String RENDER_ROUTE = "/r.html";
    private static LoadBalancer balancer;
    private LoadBalancerHandler handler= new LoadBalancerHandler();
    private RegisterWorkerHandler registerWorkerHandler= new RegisterWorkerHandler();
    private HttpServer httpServer;
    private ExecutorService executor;

    @Override
    public void run() {
        try {
            executor = Executors.newCachedThreadPool();

            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            logger.info("Creating LoadBalancer at port: " + PORT);
            httpServer.createContext(RENDER_ROUTE, handler);
            httpServer.createContext("/register", registerWorkerHandler);
            logger.info("Setup route: " + RENDER_ROUTE + " with handler " + LoadBalancerHandler.class.getName());
            httpServer.setExecutor(executor);
            httpServer.start();
            logger.info("Started loadbalancer.LoadBalancer!");

            WorkerManager.getInstance().start();

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
            logger.info("Shutting down the LoadBalancer!");
            balancer.httpServer.stop(0);
        } catch (Exception e) {
            logger.warning("There was an exception when shutting down the server, check the stacktrace");
            logger.warning(e.getMessage());
            e.printStackTrace();
        } finally {
            logger.info("Server shut down!");
        }

        synchronized (balancer) {
            balancer.notifyAll();
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("Start loadbalancer.LoadBalancer: ");
        balancer = new LoadBalancer();
        Thread serverThread = new Thread(balancer);
        serverThread.start();
        Runtime.getRuntime().addShutdownHook(new OnShutdown());
        new Shutdown().start();
        try {
            serverThread.join();
            logger.info("loadbalancer.LoadBalancer Ended (this might have been unexpected)...");
        } catch (Exception e) {
            logger.warning("loadbalancer.LoadBalancer could not be stopped properly: ");
            logger.warning(e.getMessage());
        }
    }

}

class OnShutdown extends Thread {
    public void run() {
        LoadBalancer.shutdown();
    }
}

class Shutdown extends Thread {
    public void run() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WorkerManager.getInstance().shutDown();
        LoadBalancer.shutdown();
        Runtime.getRuntime().exit(0);
    }
}
