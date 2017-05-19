package requests.metrics;

import requests.parser.Request;

import java.io.IOException;
import java.util.logging.*;

/**
 * Created by lads on 11/05/2017.
 */
public class LoggerStore extends MetricsStore {
    private static Logger logger = Logger.getLogger("Metrics");

    public LoggerStore () {
        logger.setUseParentHandlers(false);
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("metrics.log", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
    }

    @Override
    public void updateMethodCount(long threadID, long currentMethodCount) {
        if (update(currentMethodCount)) {
            logger.info("Thread: " + threadID + " has executed another " + MIN_METHOD_UPDATE + " methods for request: " + getRequestInformation(threadID));
        }

    }

    @Override
    public void storeFinalMethodCount(long threadID, long methodCount) {
        logger.info("Thread: " + threadID + " has executed " + methodCount + " methods in total for request: " + getRequestInformation(threadID));
    }

    @Override
    public void storeEstimate(Request request, long estimate) {

    }
}
