package webserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import raytracer.RayTracer;
import webserver.exception.NoModelFileException;
import webserver.exception.QueryMissingException;
import webserver.parser.QueryParser;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by lads on 10-04-2017.
 */
public class RenderRequestHandler implements HttpHandler {
    private static final String URL = "localhost:8000";
    private static Logger logger = Logger.getLogger(RenderRequestHandler.class.getName());
    private HashMap<String, String> queries;

    @Override public void handle(HttpExchange t) throws IOException {
        try {
            Date startDate = new Date();
            queries = new QueryParser(t.getRequestURI().getQuery()).toMap();
            String queryLog = "The query strings were: ";
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                queryLog += " <" + entry.getKey() + "> -> <" + entry.getValue() + ">, ";
            }
            logger.info(queryLog);
            logger.info("Creating Tracer!");
            RayTracer tracer = new RayTracer(getSceneColumns(), getSceneRows(), getWindowColumns(), getWindowRows(), getColumnOffset(), getRowOffset());
            logger.info("Reading Scene!");
            tracer.readScene(getModelFile());
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
        StringBuilder template = new StringBuilder();
        template.append("The image was generated and you can obtain in at the following url: ");
        template.append("http://" + URL + "/image?file=" + fileName);
        String response = template.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private int getSceneColumns() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("sc"));
    }

    private int getSceneRows() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("sr"));
    }

    private int getWindowColumns() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("wc"));
    }

    private int getWindowRows() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("wr"));
    }

    private int getColumnOffset() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("coff"));
    }

    private int getRowOffset() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("roff"));
    }

    private String getQueryValue(String key) throws QueryMissingException {
        String value;
        if ((value = queries.get(key)) != null ) {
            return value;
        } else throw new QueryMissingException(key);
    }

    private File getModelFile() throws NoModelFileException, QueryMissingException {
        String fileName = getQueryValue("f");
        File file = new File("/model-files/" + fileName);
        logger.info("Looking for: " + "/model-files/" + fileName);
        if (!file.exists()) {
            logger.warning("File not found!");
            throw new NoModelFileException(fileName);
        }
        logger.info("Found the file!");
        return file;
    }

    private File getDumpFile(Date startDate) {
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.S");
        String filename = "ImageResult" + formatDate.format(startDate) + ".bmp";
        return new File(filename);
    }
}
