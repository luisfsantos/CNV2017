package loadbalancer.workers;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerPolicy {
    public int secondsWithLoad;
    public int loadPercentage;

    public WorkerPolicy(int loadPercentage, int secondsWithLoad) {
        this.secondsWithLoad = secondsWithLoad;
        this.loadPercentage = loadPercentage;
    }
}
