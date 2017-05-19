package loadbalancer.workers.autoscale;

import loadbalancer.workers.WorkerPolicy;
import properties.PropertiesManager;

import java.util.*;

/**
 * Created by lads on 19/05/2017.
 */
public class AutoScaler implements Runnable{
    static PropertiesManager props = PropertiesManager.getInstance();
    public static WorkerPolicy UPSCALE_POLICY = new WorkerPolicy(props.getInteger("autoscale.upscale.load"), props.getInteger("autoscale.upscale.seconds.over.load"), 10);
    public static WorkerPolicy DOWNSCALE_POLICY = new WorkerPolicy(props.getInteger("autoscale.downscale.load"), props.getInteger("autoscale.downscale.seconds.below.load"), 1);
    static Timer monitor = new Timer();
    static ArrayList<Double> load = new ArrayList<>();
    static final int mesurePeriod = 5000; //in milliseconds

    @Override
    public void run() {
        monitor.schedule(new MonitorTask(mesurePeriod/1000), mesurePeriod, mesurePeriod);
    }

    public static Double getTotalLoad() {
        double totalLoad = 0;
        for (Double reading: load) {
            totalLoad += reading;
        }
        return totalLoad;
    }

    public static Double getUpscaleLoad() {
        double totalLoad = 0;
        int upscaleEntries = UPSCALE_POLICY.secondsWithLoad/mesurePeriod;
        int pos = 0;
        for (Double reading: load) {
            if (pos > upscaleEntries)
                totalLoad += reading;
            pos++;
        }
        return totalLoad;
    }


    public static Double getDownScaleLoad() {
        double totalLoad = 0;
        int downScaleEntries = DOWNSCALE_POLICY.secondsWithLoad/mesurePeriod;
        int pos = 0;
        for (Double reading: load) {
            if (pos > downScaleEntries)
                totalLoad += reading;
            pos++;
        }
        return totalLoad;
    }

}
