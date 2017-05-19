package storage.estimation;

import requests.parser.Request;

import java.util.TimerTask;

/**
 * Created by lads on 19/05/2017.
 */
public class EstimatorTask extends TimerTask {
    @Override
    public void run() {
        //check database of requests, for each where estimate is different (0.1) from the estimate pass it to the estimates table
    }

    public RequestEstimate transformRequest(Request request) {
        return null;
    }
}
