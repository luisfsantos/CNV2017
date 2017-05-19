package loadbalancer.workers;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import properties.PropertiesManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerManager {
    private final static Logger logger = Logger.getLogger(WorkerManager.class.getName());

    private static WorkerPolicy UPSCALE_POLICY = new WorkerPolicy(85, 180);
    private static WorkerPolicy DOWNSCALE_POLICY = new WorkerPolicy(40, 360);
    static AmazonEC2 ec2 = null;
    private static WorkerManager instance = new WorkerManager();
    private List<WorkerWrapper> workers = new ArrayList<>();

    private WorkerManager(){
    }

    private void init(){
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(credentialsProvider).build();

        queryForWorkers();
    }

    public void start() {
        init();
    }

    public static synchronized WorkerManager getInstance(){
        return instance;
    }

    public void addWorker(WorkerWrapper worker) {
        workers.add(worker);
    }

    public void createWorker(long complexity) {
        if (complexity/WorkerWrapper.MAX_LOAD > 0) {
            addWorker(WorkerWrapper.requestNewWorker(ec2));
        }
    }

    private void queryForWorkers() {
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        PropertiesManager props = PropertiesManager.getInstance();
        for (Reservation reservation : reservations) {
            for( Instance instance : reservation.getInstances()){
                if (instance.getState().getName().equals(InstanceStateName.Running.toString()) &&
                        instance.getImageId().equals(props.getString("render.image.id")) &&
                        instance.getInstanceType().equals(props.getString("render.instance.type")) &&
                        instance.getIamInstanceProfile().getArn().equals(props.getString("render.iam.role.arn"))) {
                    workers.add(new WorkerWrapper(instance.getPublicIpAddress(), instance.getInstanceId()));
                    logger.info("Adding " + instance.getInstanceId() + " " + instance.getState().getName() + " " +
                            instance.getImageId() + " " +
                            instance.getInstanceType() + " " +
                            instance.getIamInstanceProfile().getArn());
                }
            }
        }
        if (workers.isEmpty()) {
            addWorker(WorkerWrapper.requestNewWorker(ec2));
        }
    }

    public void shutDown() {
        for (WorkerWrapper worker: workers) {
            if (worker.isActive()) {
                worker.shutDown(ec2);
            }
        }
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
        createWorker(complexity);
        Collections.sort(workers, WorkerWrapper.COMPARATOR_BY_LOAD);
        int best = 0;
        WorkerWrapper choosen = workers.get(best);
        while (choosen.isTerminated())
            best++;
            choosen = workers.get(best);
        return choosen;
    }


}
