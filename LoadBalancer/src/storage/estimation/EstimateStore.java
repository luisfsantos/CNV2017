package storage.estimation;

import requests.parser.Request;

/**
 * Created by lads on 17/05/2017.
 */
public abstract class EstimateStore {

    public abstract long requestEstimate(Request request);

    public abstract void processEstimates();
}
