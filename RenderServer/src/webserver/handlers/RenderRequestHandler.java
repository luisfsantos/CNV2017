package webserver.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import raytracer.RayTracer;
import requests.metrics.Storage;
import requests.parser.QueryParser;
import requests.parser.Request;
import requests.exception.NoModelFileException;
import requests.exception.QueryMissingException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by lads on 10-04-2017.
 */
public class RenderRequestHandler implements HttpHandler {
    private static final String URL = "localhost:8000";
    private static Logger logger = Logger.getLogger(RenderRequestHandler.class.getName());

    @Override public void handle(HttpExchange t) throws IOException {
        try {
            Request request;
            Date startDate = new Date();
            QueryParser queryParser = new QueryParser(t.getRequestURI().getQuery());
            request = queryParser.getRequest();
            Storage.getStore().setRequestInformation(Thread.currentThread().getId(), request);
            logger.info("Request : " + request.toString());
            logger.info("Creating Tracer!");
            RayTracer tracer = new RayTracer(request.getSceneColumns(),
                    request.getSceneRows(),
                    request.getWindowColumns(),
                    request.getWindowRows(),
                    request.getColumnOffset(),
                    request.getRowOffset());
            logger.info("Reading Scene!");
            tracer.readScene(request.getSceneFile());
            logger.info("Writing to file!");
            File outFile = getDumpFile(startDate);
            String fileName = outFile.getName();
            tracer.draw(outFile);
            logger.info("File rendered");
            renderResponse(t, fileName);

        } catch (NoModelFileException | QueryMissingException e) {
            t.sendResponseHeaders(400, e.getMessage().length());
            OutputStream os = t.getResponseBody();
            os.write(e.getMessage().getBytes());
            os.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.warning("Processing request went awry: ");
            logger.warning(stackTrace);
            t.sendResponseHeaders(400, stackTrace.length());
            OutputStream os = t.getResponseBody();
            os.write(stackTrace.getBytes());
            os.close();
        }
    }

    private void renderResponse(HttpExchange t, String fileName) throws IOException {
        Headers headers = t.getResponseHeaders();
        headers.add("Content-Type", "image/png");
        File file = new File(fileName);
        byte[] bytes  = new byte [(int)file.length()];
        logger.info(file.getAbsolutePath());
        logger.info("length:" + file.length());

        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(bytes, 0, bytes.length);

        t.sendResponseHeaders(200, file.length());
        OutputStream outputStream = t.getResponseBody();
        outputStream.write(bytes, 0, bytes.length);
        outputStream.close();
    }

    private File getDumpFile(Date startDate) {
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.S");
        String filename = "ImageResult" + formatDate.format(startDate) + ".bmp";
        return new File(filename);
    }
}
