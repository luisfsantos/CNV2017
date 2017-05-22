package storage.estimation;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import requests.Storage;
import requests.exception.QueryMissingException;
import requests.metrics.RequestMetrics;
import requests.parser.QueryParser;
import requests.parser.Request;

import java.util.Iterator;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by lads on 19/05/2017.
 */
public class EstimatorTask extends TimerTask {
    private static Logger logger = Logger.getLogger(EstimateStore.class.getName());
    EstimateStore store;
    double acceptableAmount = 0.1;

    public EstimatorTask() {
        this.store = new EstimateStore();
    }

    @Override
    public void run() {
        //check database of requests, for each where estimate is different (0.1) from the estimate pass it to the estimates table
        /*try {
            Request request1 = new QueryParser("sc=10&sr=10&wc=100&wr=100&coff=0&roff=0&f=test03.txt").getRequest();
            Request request2 = new QueryParser("sc=7000&sr=7000&wc=7000&wr=7000&coff=350&roff=350&f=test03.txt").getRequest();
            Request request3 = new QueryParser("sc=700&sr=700&wc=700&wr=700&coff=35&roff=35&f=test03.txt").getRequest();
            RequestEstimate estimate1 = new RequestEstimate(request1, 10000);
            RequestEstimate estimate2 = new RequestEstimate(request2, 20000);
            RequestEstimate estimate3 = new RequestEstimate(request3);

            store.storeEstimate(estimate1);
            store.storeEstimate(estimate2);

            System.out.println(store.requestEstimate(request3));

        } catch (QueryMissingException e) {
            e.printStackTrace();
        }*/
        logger.info("Estimating metrics.");
        PaginatedScanList<RequestMetrics> metricsToWorkOn = Storage.getMetricsStore().getRequestMetricsToProcess();
        if (metricsToWorkOn == null || metricsToWorkOn.isEmpty()) {
            logger.info("There are no metrics to process.");
            return;
        }
        for (RequestMetrics metric: metricsToWorkOn) {
            double guess = metric.getEstimatedMethods();
            double real = metric.getFinalMethods();
            double ratio = guess / real;
            if (ratio >= 1+acceptableAmount || ratio <= 1-acceptableAmount) {
                store.storeEstimate(transformRequest(metric));
            }
            Storage.getMetricsStore().deleteMetric(metric);

        }
    }

    public RequestEstimate transformRequest(RequestMetrics metrics) {
        return new RequestEstimate(
                metrics.getRequest(),
                metrics.getFinalMethods() / metrics.getRequest().getImageArea()
        );
    }
}
