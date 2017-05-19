package loadbalancer.workers.autoscale;

import loadbalancer.workers.WorkerPolicy;
import properties.PropertiesManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lads on 19/05/2017.
 */
public class AutoScaler implements Runnable{
    static PropertiesManager props = PropertiesManager.getInstance();
    private static WorkerPolicy UPSCALE_POLICY = new WorkerPolicy(props.getInteger("autoscale.upscale.load"), props.getInteger("autoscale.upscale.seconds.over.load"));
    private static WorkerPolicy DOWNSCALE_POLICY = new WorkerPolicy(props.getInteger("autoscale.downscale.load"), props.getInteger("autoscale.downscale.seconds.below.load"));
    static Timer monitor = new Timer();

    @Override
    public void run() {
        monitor.schedule(new MonitorTask(), 1000, 1000);
    }

}
