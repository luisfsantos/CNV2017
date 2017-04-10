package webserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.awt.windows.ThemeReader;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by lads on 10-04-2017.
 */
public class RenderRequestHandler implements HttpHandler {

    @Override public void handle(HttpExchange t) throws IOException {
        String response = "This was the query:" + t.getRequestURI().getQuery()
                + "##";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
