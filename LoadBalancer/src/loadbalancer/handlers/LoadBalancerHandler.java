package loadbalancer.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import loadbalancer.LoadBalancer;
import webserver.parser.QueryParser;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;

/**
 * Created by joao on 07-05-2017.
 */
public class LoadBalancerHandler implements HttpHandler{
    private static Logger logger = Logger.getLogger(LoadBalancerHandler.class.getName());
    private HashMap<HttpExchange ,HashMap<String, String>> queries;
    ArrayList<String> instanceIds = new ArrayList<String>();

    static AmazonEC2 ec2;


    public LoadBalancerHandler(){
        super();
        init();
    }

    private void init(){
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        HashMap<String,String> query = QueryParser.toMap(t.getRequestURI().getQuery());
        queries.put(t, query);

        String queryLog = "The query strings were: ";
        for (Map.Entry<String, String> entry : query.entrySet()) {
            queryLog += " <" + entry.getKey() + "> -> <" + entry.getValue() + ">, ";
        }
        logger.info(queryLog);
    }

    private int getNumberMachinesAlive() {
        int alive = 0;
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            for( Instance instance : reservation.getInstances()){
                if(instance.getState().getCode() == 16) alive++;
            }
        }
        return alive++;
    }

    private void launchInstance(){
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
                ec2.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();
        instanceIds.add(newInstanceId);
    }

    private void terminateInstance(String id){
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(id);
        ec2.terminateInstances(termInstanceReq);
    }

}
