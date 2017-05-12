package loadbalancer.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Created by joao on 08-05-2017.
 */



public class TestHandler implements HttpHandler {


    private static Logger logger = Logger.getLogger(TestHandler.class.getName());
    List<String> history = new ArrayList<String>();

    static AmazonEC2 ec2;

    public TestHandler(){
        super();
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
        /*String query = t.getRequestURI().getQuery();
        if(history.contains(query)){
            alreadyHas(t, query);
        }else{
            history.add(query);
            addSuccessful(t, query);
        }*/

        machinesAlive(t);

    }

    private void machinesAlive(HttpExchange t) throws IOException {
        StringBuilder template = new StringBuilder();
        int n = getNumberMachinesAlive();
        template.append(n);
        template.append(" machines alive");
        String response = template.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void alreadyHas(HttpExchange t, String query) throws IOException{
        StringBuilder template = new StringBuilder();
        template.append("Server already has ");
        template.append(query);
        String response = template.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void addSuccessful(HttpExchange t, String query) throws IOException{
        StringBuilder template = new StringBuilder();
        template.append("Server added query : ");
        template.append(query);
        String response = template.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
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
}
