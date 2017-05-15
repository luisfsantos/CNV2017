package requests.metrics;

/**
 * Created by lads on 11/05/2017.
 */
public class Storage {
    /* When using a different storage format change the type of store here and all stores will be changed */

    private static MetricsStore store = new DynamoStore();

    /**
     *
     * @return the storage being used for the programme
     */
    public static synchronized MetricsStore getMetricsStore() { return store; }
}
