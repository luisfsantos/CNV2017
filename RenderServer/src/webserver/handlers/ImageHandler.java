package webserver.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import webserver.parser.QueryParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by lads on 10-04-2017.
 */
public class ImageHandler implements HttpHandler {

    @Override public void handle(HttpExchange he) throws IOException {
        Headers headers = he.getResponseHeaders();
        headers.add("Content-Type", "image/png");
        HashMap<String, String> queries = QueryParser.toMap(he.getRequestURI().getQuery());
        File file = new File(queries.get("file"));
        byte[] bytes  = new byte [(int)file.length()];
        System.out.println(file.getAbsolutePath());
        System.out.println("length:" + file.length());

        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(bytes, 0, bytes.length);

        he.sendResponseHeaders(200, file.length());
        OutputStream outputStream = he.getResponseBody();
        outputStream.write(bytes, 0, bytes.length);
        outputStream.close();
    }
}
