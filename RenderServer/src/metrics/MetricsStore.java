package metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lads on 11/05/2017.
 */
public abstract class MetricsStore {
    Map<Long, String> requestInformation = new HashMap<>();

    public void setRequestInformation(long threadID, String requestInformation) {
        this.requestInformation.put(threadID, requestInformation);
    }

    public String getRequestInformation(long threadID) {
        return this.requestInformation.get(threadID);
    }

    /**
     *  Store the number of methods executed in a given interval specified in seconds.
     * @param threadID
     * @param methodCount
     * @param interval the interval (in seconds)
     */
    public abstract void storeIntervalMethodCount(long threadID, long methodCount, int interval);

    /**
     * Store the number of methods executed in total at the end of the request.
     * @param threadID
     * @param methodCount
     */
    public abstract void storeFinalMethodCount(long threadID, long methodCount);

}
