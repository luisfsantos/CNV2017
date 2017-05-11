package metrics;

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
    public void storeIntervalMethodCount(long threadID, long methodCount, int interval) {
        logger.info("Thread: " + threadID + " has executed " + methodCount + " methods in " + " seconds.");
    }

    @Override
    public void storeFinalMethodCount(long threadID, long methodCount) {
        logger.info("Thread: " + threadID + " has executed " + methodCount + " methods in total.");
    }
}
