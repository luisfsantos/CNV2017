package storage.estimation;

import requests.exception.QueryMissingException;
import requests.metrics.RequestMetrics;
import requests.parser.QueryParser;
import requests.parser.Request;

import java.util.TimerTask;

/**
 * Created by lads on 19/05/2017.
 */
public class EstimatorTask extends TimerTask {
    EstimateStore store;

    public EstimatorTask() {
        this.store = new EstimateStore();
    }

    @Override
    public void run() {
        //check database of requests, for each where estimate is different (0.1) from the estimate pass it to the estimates table
        try {
            Request request1 = new QueryParser("sc=10&sr=10&wc=100&wr=100&coff=0&roff=0&f=test03.txt").getRequest();
            Request request2 = new QueryParser("sc=7000&sr=7000&wc=7000&wr=7000&coff=350&roff=350&f=test03.txt").getRequest();

            RequestEstimate estimate1 = new RequestEstimate(request1, 10000);
            RequestEstimate estimate2 = new RequestEstimate(request2, 20000);

            store.storeEstimate(estimate1);
            store.storeEstimate(estimate2);

            System.out.println(store.requestEstimate(request1));

        } catch (QueryMissingException e) {
            e.printStackTrace();
        }
    }

    public RequestEstimate transformRequest(RequestMetrics metrics) {
        return new RequestEstimate(
                metrics.getRequest(),
                metrics.getFinalMethods() / metrics.getRequest().getImageArea()
        );
    }
}
