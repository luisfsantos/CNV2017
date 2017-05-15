package requests.metrics;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import requests.parser.Request;

import java.util.logging.Logger;

/**
 * Created by lads on 11/05/2017.
 */
public class DynamoStore extends MetricsStore {

    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    private static Logger logger = Logger.getLogger(DynamoStore.class.getName());

    public DynamoStore() {
        init();
    }

    public void init() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentialsProviderChain credentialsProvider;
        try {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }

        client = AmazonDynamoDBClientBuilder
                .standard()
                //TODO Remove endpoint config in production
                //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Regions.DEFAULT_REGION.getName()))
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();
        mapper = new DynamoDBMapper(client);
        CreateTableRequest req = mapper.generateCreateTableRequest(RequestMetrics.class);
        // Table provision throughput is still required since it cannot be specified in your POJO
        req.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
        // Fire off the CreateTableRequest using the low-level client
        TableUtils.createTableIfNotExists(client, req);
        try {
            TableUtils.waitUntilActive(client, RequestMetrics.TABLE);
        } catch (InterruptedException e) {
            logger.warning("Could not wait for table to be active.");
            logger.warning(e.getMessage());
        }

    }

    @Override
    public void updateMethodCount(long threadID, long currentMethodCount) {
        if (update(currentMethodCount)) {
            Request request = getRequestInformation(threadID);
            RequestMetrics requestMetrics = mapper.load(RequestMetrics.class, request.getRequestID());
            if (requestMetrics == null) {
                requestMetrics = new RequestMetrics(request);
            } else {
                long currentMethods = requestMetrics.getCurrentMethods();
                currentMethods += MIN_METHOD_UPDATE;
                requestMetrics.setCurrentMethods(currentMethods);
            }
            mapper.save(requestMetrics);
        }
    }

    @Override
    public void storeFinalMethodCount(long threadID, long methodCount) {
        Request request = getRequestInformation(threadID);
        RequestMetrics requestMetrics = mapper.load(RequestMetrics.class, request.getRequestID());
        if (requestMetrics == null) {
            requestMetrics = new RequestMetrics(request);
        } else {
           requestMetrics.setFinalMethods(methodCount);
        }
        mapper.save(requestMetrics);
        //TODO mark request as done...
    }

    @Override
    public void storeEstimate(String requestID, long estimate) {

    }
}
