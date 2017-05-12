package metrics;

import webserver.parser.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lads on 11/05/2017.
 */
public abstract class MetricsStore {
    Map<Long, Query> requestInformation = new HashMap<>();
    protected final long MIN_METHOD_UPDATE = 100000;

    public void setRequestInformation(long threadID, Query query) {
        this.requestInformation.put(threadID, query);
    }

    public Query getRequestInformation(long threadID) {
        return this.requestInformation.get(threadID);
    }

    protected boolean update(long currentMethodCount) {
        return (currentMethodCount % MIN_METHOD_UPDATE) == 0;
    }

    /**
     *  Update the number of methods currently executed if it is a multiple of @link{MIN_METHOD_UPDATE}
     * @param threadID
     * @param currentMethodCount
     */
    public abstract void updateMethodCount(long threadID, long currentMethodCount);

    /**
     * Store the number of methods executed in total at the end of the request.
     * @param threadID
     * @param methodCount
     */
    public abstract void storeFinalMethodCount(long threadID, long methodCount);

}
