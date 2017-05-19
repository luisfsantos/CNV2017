package storage.estimation;

import loadbalancer.workers.autoscale.MonitorTask;
import requests.parser.Request;

import java.util.Timer;

/**
 * Created by lads on 19/05/2017.
 */
public class Estimator implements Runnable {

    static Timer estimator = new Timer();

    @Override
    public void run() {
        estimator.schedule(new EstimatorTask(), 1000, 1000);
    }
}
