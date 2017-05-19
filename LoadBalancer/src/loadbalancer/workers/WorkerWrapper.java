package loadbalancer.workers;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import properties.PropertiesManager;

import requests.parser.Request;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by lads on 15/05/2017.
 */
public class WorkerWrapper {
    private final static Logger logger = Logger.getLogger(WorkerWrapper.class.getName());

    final static long MAX_LOAD = 10000000L; //One hundered million
    String address;
    String workerID;
    long currentLoad = 0;
    WorkerStatus status;
    HashMap<String, Request> currentRequests = new HashMap<>();
    HashMap<String, Long> requestEstimatedComplexity = new HashMap<>();
    Timer checkStatus = new Timer();

    public WorkerWrapper(String ip, String workerID) {
        this.address = ip + ":" + PropertiesManager.getInstance().getString("render.port");
        this.workerID = workerID;
        status = WorkerStatus.ACTIVE;
        checkStatus.schedule(new UpdateStatusTask(WorkerManager.ec2, this), 60 * 1000);
    }

    private WorkerWrapper(String workerID) {
        this.workerID = workerID;
        status = WorkerStatus.STARTING;
        checkStatus.schedule(new CheckStatusTask(WorkerManager.ec2, this), 60 * 1000);
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

    public synchronized void shutDown(AmazonEC2 client) {
        status = WorkerStatus.STOPPING;
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(workerID);
        client.terminateInstances(termInstanceReq);
    }

    public static WorkerWrapper requestNewWorker(AmazonEC2 client) {
        //TODO launch a new EC2 intance in AWS
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        // TODO: configure to use your AMI, key and security group */
        PropertiesManager props = PropertiesManager.getInstance();
        runInstancesRequest.withImageId(props.getString("render.image.id"))
                .withInstanceType(props.getString("render.instance.type"))
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(props.getString("render.key.name"))
                .withSecurityGroups(props.getString("render.security.group"))
                .withIamInstanceProfile(new IamInstanceProfileSpecification().withName(props.getString("render.iam.role.name")))
        ;
        RunInstancesResult runInstancesResult =
                client.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();
        String newInstanceIP = runInstancesResult.getReservation().getInstances()
                .get(0).getPublicIpAddress();
        logger.info("made a new instance with ID " + newInstanceId + " and IP " + newInstanceIP);

        return new WorkerWrapper(newInstanceIP, newInstanceId);
    }

    private boolean updateState(AmazonEC2 client){
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(this.workerID);
        DescribeInstancesResult describeInstancesResult = client.describeInstances(describeInstancesRequest);
        InstanceState state = describeInstancesResult.getReservations().get(0).getInstances().get(0).getState();
        if(state.getName().equals(InstanceStateName.Pending.toString())){
            this.status = WorkerStatus.STARTING;
            return false;
        }
        else if(state.getName().equals(InstanceStateName.Running.toString())){
            if (address == null) this.address = describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
            this.status = WorkerStatus.STARTED;
            return true;
        }
        return false;
    }

    public String getAddress() {
        return address;
    }

    class CheckStatusTask extends TimerTask {

        AmazonEC2 client;
        WorkerWrapper worker;

        public CheckStatusTask(AmazonEC2 client, WorkerWrapper worker){
            this.client = client;
            this.worker = worker;
        }

        public void run() {
            if(worker.status.equals(WorkerStatus.STARTING))
                if(worker.updateState(client)){
                    this.cancel();
                    worker.checkStatus.schedule(new UpdateStatusTask(this.client, this.worker), 60 * 1000);
                }
        }
    }

    class UpdateStatusTask extends TimerTask {

        AmazonEC2 client;
        WorkerWrapper worker;

        public UpdateStatusTask(AmazonEC2 client, WorkerWrapper worker){
            this.client = client;
            this.worker = worker;
        }

        public void run() {
                //TODO health check
        }
    }
}
