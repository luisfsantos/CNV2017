import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;
import requests.Storage;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;

public class MetricMethodCount {
//    private static int m_count = 0;
    private static HashMap<Long, Integer> m_count =  new HashMap<>();

    public static void main(String[] args) {

        String outputFolder = args[1];

        if (args[0].endsWith(".class")) {
            if (!args[0].contains(System.getProperty("file.separator"))) {
                instrumentClass(args[0], ".", outputFolder);
            } else {
                int index = args[0].lastIndexOf(System.getProperty("file.separator"));
                String sourceFolder = args[0].substring(0, index);
                String classFile = args[0].substring(index + 1);
                instrumentClass(classFile, sourceFolder, outputFolder);
            }
        } else {
            File file_in = new File(args[0]);
            String infilenames[] = file_in.list();
            if (infilenames == null) {
                return; //is not a directory
            }
            for (int i = 0; i < infilenames.length; i++) {
                String infilename = infilenames[i];
                if (infilename.endsWith(".class")) {
                    instrumentClass(infilename, args[0], outputFolder);
                }
            }
        }
    }

    private static synchronized void instrumentClass(String filename, String srcFolder, String destFolder) {
        // create class info object
        ClassInfo ci;
        ci = new ClassInfo(srcFolder + System.getProperty("file.separator") + filename);

        // loop through all the routines
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            routine.addBefore("MetricMethodCount", "mcount", new Integer(1));
            if (routine.getMethodName().contentEquals("draw")) {
                routine.addAfter("MetricMethodCount", "storeMethodCount", ci.getClassName());
            }
        }
        ci.write(destFolder + System.getProperty("file.separator") + filename);
    }

    public static synchronized void storeMethodCount(String foo) {
        Long id = Thread.currentThread().getId();
        Storage.getMetricsStore().storeFinalMethodCount(id, m_count.get(Thread.currentThread().getId()));
        m_count.put(id, 0);
    }

    public static synchronized void mcount(int incr) {
        Long id = Thread.currentThread().getId();
        Integer prev_mcount = m_count.get(id);
        if (prev_mcount == null) {
            m_count.put(id, 0);
        } else {
            m_count.put(id, prev_mcount + 1);
            Storage.getMetricsStore().updateMethodCount(id, prev_mcount+1);
        }
    }
}
