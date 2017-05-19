package loadbalancer.workers.autoscale;

import loadbalancer.workers.WorkerManager;
import loadbalancer.workers.WorkerWrapper;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by lads on 19/05/2017.
 */
public class MonitorTask extends TimerTask {
    private final static Logger logger = Logger.getLogger(MonitorTask.class.getName());

    private int maxArraySize;
    private int minArraySize;
    private int position = 0;

    public MonitorTask(int period) {
        this.maxArraySize = Math.max(AutoScaler.UPSCALE_POLICY.secondsWithLoad, AutoScaler.DOWNSCALE_POLICY.secondsWithLoad)/period;
        this.minArraySize = Math.min(AutoScaler.UPSCALE_POLICY.secondsWithLoad, AutoScaler.DOWNSCALE_POLICY.secondsWithLoad)/period;
    }

    @Override
    public void run() {
        if (AutoScaler.load.size() >= minArraySize) {
            if (AutoScaler.load.size() >= maxArraySize) {
                AutoScaler.load.set(position, WorkerManager.getInstance().getAverageLoad());
            } else {
                AutoScaler.load.add(WorkerManager.getInstance().getAverageLoad());
            }
            if (AutoScaler.getUpscaleLoad() > AutoScaler.UPSCALE_POLICY.loadPercentage) {
                WorkerManager.getInstance().createWorker(WorkerWrapper.MAX_LOAD);
                AutoScaler.load = new ArrayList<>(0);
            } else if (AutoScaler.getDownScaleLoad() < AutoScaler.DOWNSCALE_POLICY.loadPercentage) {
                WorkerManager.getInstance().shutDownLeastWorker();
                AutoScaler.load = new ArrayList<>(0);
            }
        } else {
            AutoScaler.load.add(WorkerManager.getInstance().getAverageLoad());
        }
        position = (position + 1) % maxArraySize;

    }
}
