package webserver.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import webserver.parser.QueryParser;

import java.io.*;
import java.util.HashMap;

/**
 * Created by lads on 19/04/2017.
 */
public class TestHandler implements HttpHandler {

    @Override public void handle(HttpExchange he) throws IOException {
        String response = "Test URL";

        he.sendResponseHeaders(200, response.length());
        OutputStream outputStream = he.getResponseBody();
        outputStream.write(response.getBytes(), 0, response.getBytes().length);
        outputStream.close();
    }
}
