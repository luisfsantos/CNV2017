package storage.estimation;

import loadbalancer.workers.autoscale.MonitorTask;
import properties.PropertiesManager;
import requests.parser.Request;

import java.util.Timer;

/**
 * Created by lads on 19/05/2017.
 */
public class Estimator implements Runnable {

    static Timer estimator = new Timer();
    static int PERIOD = PropertiesManager.getInstance().getInteger("estimate.period.milliseconds");
    @Override
    public void run() {
        estimator.schedule(new EstimatorTask(), PERIOD, PERIOD);
    }
}
