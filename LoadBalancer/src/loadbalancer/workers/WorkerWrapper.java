package loadbalancer.workers;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import requests.parser.Request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerWrapper {
    private final static Logger logger = Logger.getLogger(WorkerWrapper.class.getName());

    private final static long MAX_LOAD = 10000000L; //One hundered million
    String address;
    String workerID;
    long currentLoad = 0;
    WorkerStatus status;
    HashMap<String, Request> currentRequests = new HashMap<>();
    HashMap<String, Long> requestEstimatedComplexity = new HashMap<>();

    public WorkerWrapper(String address, String workerID) {
        this.address = address;
        this.workerID = workerID;
        status = WorkerStatus.ACTIVE;
    }

    public synchronized void addRequest(Request request, long estimatedComplexity) {
        currentLoad += estimatedComplexity;
        requestEstimatedComplexity.put(request.getRequestID(), estimatedComplexity);
        currentRequests.put(request.getRequestID(), request);
    }

    /**
     * Gets the estimated load as a percentage
     * @return the current estimated load in terms of percentage
     */
    public long getLoad() {
        return currentLoad/MAX_LOAD * 100;
    }

    public synchronized void finishRequest(Request request) {
        long complexity = requestEstimatedComplexity.get(request.getRequestID());
        currentLoad -= complexity;
        requestEstimatedComplexity.remove(request.getRequestID());
        currentRequests.remove(request.getRequestID());
    }

    public boolean isActive() {
        return status.equals(WorkerStatus.ACTIVE);
    }

    public boolean isTerminated() {
        return status.equals(WorkerStatus.STOPPING);
    }

    public synchronized void shutDown() {
        status = WorkerStatus.STOPPING;
        //TODO Launch a request to Terminate this instance of worker
    }

    public static String requestNewWorker(AmazonEC2 client) {
        //TODO launch a new EC2 intance in AWS
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        // TODO: configure to use your AMI, key and security group */
        runInstancesRequest.withImageId("ami-082a5f1e")
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName("CNV")
                .withSecurityGroups("CNV-ssh+http")
        ;
        RunInstancesResult runInstancesResult =
                client.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();
        return newInstanceId;
    }

    public String getAddress() {
        return address;
    }
}
