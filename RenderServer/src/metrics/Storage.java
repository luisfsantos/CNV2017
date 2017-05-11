package metrics;

/**
 * Created by lads on 11/05/2017.
 */
public class Storage {
    /* When using a different storage format change the type of store here and all stores will be changed */

    private static LoggerStore store = new LoggerStore();

    /**
     *
     * @return the storage being used for the programme
     */
    public static synchronized LoggerStore getStore() {
        return store;
    }
}
