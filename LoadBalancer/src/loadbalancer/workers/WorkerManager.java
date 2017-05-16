package loadbalancer.workers;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

import java.util.*;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerManager {
    private static WorkerPolicy UPSCALE_POLICY = new WorkerPolicy(85, 180);
    private static WorkerPolicy DOWNSCALE_POLICY = new WorkerPolicy(40, 360);
    static AmazonEC2 ec2;
    private static WorkerManager instance;
    private List<WorkerWrapper> workers = new ArrayList<>();

    private WorkerManager(){
        init();
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

    public static synchronized WorkerManager getInstance(){
        if(instance == null){
            instance = new WorkerManager();
        }
        return instance;
    }

    public void addWorker(WorkerWrapper worker) {
        workers.add(worker);
    }

    public void createWorker(long complexity) {
        for (int i = 0; i < WorkerWrapper.MAX_LOAD/complexity; i++) {
            addWorker(WorkerWrapper.requestNewWorker(ec2));
        }

    }

    private void queryForWorkers() {
        //FIXME look for workers and make workerwrappers if they are of the ami we want
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            for( Instance instance : reservation.getInstances()){
                //TODO get all instances that are using the right ami and are alive and create workers from these,
                //TODO this is supposed to be an inicial setup of the loadbalancer
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
        //TODO add this estimated complexity to the actual complexity of the worker wrapper so as to know the current complexity
        //TODO get a worker based on complexity of the given task
        return workers.get(new Random().nextInt(workers.size()));
    }


}
