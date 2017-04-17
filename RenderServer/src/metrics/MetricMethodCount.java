package metrics;

import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.*;

/**
 * Created by Catarina on 17/04/2017.
 */
public class MetricMethodCount {

    private static Logger logger = Logger.getLogger(MetricMethodCount.class.getName());
    private static int m_count = 0;

    public static void main(String[] argv) {

        LogManager.getLogManager().reset();
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setUseParentHandlers(false);
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("metrics.log", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(fileHandler);


        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();

        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
                // create class info object
                ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);

                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("MetricMethodCount", "mcount", new Integer(1));
                }
                ci.addAfter("MetricMethodCount", "storeMethodCount", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static synchronized void storeMethodCount() {
        logger.info(Thread.currentThread().getName() + " Methods executed: " + m_count);
    }

    public static synchronized void mcount(int incr) {
        m_count += incr;
    }
}
