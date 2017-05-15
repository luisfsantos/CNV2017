package loadbalancer.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerManager {
    private static WorkerPolicy UPSCALE_POLICY = new WorkerPolicy(85, 180);
    private static WorkerPolicy DOWNSCALE_POLICY = new WorkerPolicy(40, 360);

    private static WorkerManager instance;
    private List<WorkerWrapper> workers = new ArrayList<>();

    private WorkerManager(){}

    public static synchronized WorkerManager getInstance(){
        if(instance == null){
            instance = new WorkerManager();
        }
        return instance;
    }

    public void addWorker(WorkerWrapper worker) {
        workers.add(worker);
    }

    public void addWorkerFromIP(String IP) {
        //TODO build worker from IP and save to the set
    }

    /**
     *
     * @return random @link{WorkerWrapper}
     */
    public WorkerWrapper getWorker() {
        return workers.get(new Random().nextInt(workers.size()));
    }

    /**
     *
     * @param complexity
     * @return return the best worker for a given complexity
     */
    public synchronized WorkerWrapper getWorker(long complexity) {
        //TODO add this estimated complexity to the actual complexity of the worker wrapper so as to know the current complexity
        //TODO get a worker based on complexity of the given task
        return workers.get(new Random().nextInt(workers.size()));
    }


}
