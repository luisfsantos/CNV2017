package webserver;

import com.sun.net.httpserver.HttpServer;
import webserver.handlers.RenderRequestHandler;

import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new RenderRequestHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

}
