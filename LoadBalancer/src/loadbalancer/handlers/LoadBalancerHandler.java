package loadbalancer.handlers;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import requests.parser.QueryParser;
import requests.parser.Request;
import requests.exception.QueryMissingException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by joao on 07-05-2017.
 */
public class LoadBalancerHandler implements HttpHandler{
    private static Logger logger = Logger.getLogger(LoadBalancerHandler.class.getName());
    private static HashMap<HttpExchange ,Request> queries = new HashMap<>();
    public static ArrayList<String> instanceIP = new ArrayList<String>();
    private static ArrayList<String> instanceIds = new ArrayList<String>();
    private static HashMap<String, Long> currentComplexity = new HashMap<>();

    static AmazonEC2 ec2;


    public LoadBalancerHandler(){
        super();
        init();
    }

    private void init(){
        //FIXME
        instanceIP.add("localhost");
        //FIXME
        logger.info("add localhost");
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(credentialsProvider).build();
        logger.info("done creating AmazonEC2Client");
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            Request request;
            logger.info("Parsing Query...");
            QueryParser queryParser = new QueryParser(t.getRequestURI().getQuery());
            request = queryParser.getRequest();
            logger.info("retrieved request " + request);
            queries.put(t, request);
            logger.info("put http exchange and request in queries");
            request.setRequestID(UUID.randomUUID().toString());
            logger.info("added reqest id: " + request.getRequestID());
            long complexity  = estimateComplexity(request);
            logger.info("estimating complexity: " + complexity);
            byte[] buffer = sendRequest(request, getWorker(complexity));

            //TODO Get the file back!!!

            t.sendResponseHeaders(200, buffer.length);
            Headers headers = t.getResponseHeaders();
            headers.add("Content-Type", "image/png");
            OutputStream outputStream = t.getResponseBody();
            outputStream.write(buffer);
            outputStream.close();

            //TODO markdown the instance complexity etc

        } catch (QueryMissingException e) {
            t.sendResponseHeaders(400, e.getMessage().length());
            OutputStream os = t.getResponseBody();
            os.write(e.getMessage().getBytes());
            os.close();
        }


    }

    private String getWorker(long complexity) {
        //FIXME given the complexity get the best worker for the job
        return instanceIP.get(0);
    }

    private byte[] sendRequest(Request request, String ip) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://" + ip + ":8080/r.html?" + request.getRequestHash());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoInput(true);

            //Get Response
            BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
            logger.info("The reponse has length: " +connection.getContentLength());
            byte[] buffer = new byte[connection.getContentLength()];
            is.read(buffer);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private long estimateComplexity(Request request) {
        //TODO estimate the complexity and write to DynamoDB
        return Long.divideUnsigned(request.getSceneArea()*2L + request.getImageArea()*3L, 2L);
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
