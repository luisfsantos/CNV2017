package loadbalancer.workers;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerPolicy {
    public int secondsWithLoad;
    public int loadPercentage;
    public int workers;

    public WorkerPolicy(int loadPercentage, int secondsWithLoad, int workers) {
        this.secondsWithLoad = secondsWithLoad;
        this.loadPercentage = loadPercentage;
        this.workers = workers;
    }
}
