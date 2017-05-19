package storage.estimation;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import requests.parser.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by lads on 17/05/2017.
 */
public class EstimateStore {
    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    private static final double TOLERANCE = 0.1;
    private static Logger logger = Logger.getLogger(EstimateStore.class.getName());

    public EstimateStore() {
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
        } catch (Exception e) {
            throw new RuntimeException("Error loading credentials", e);
        }

        client = AmazonDynamoDBClientBuilder
                .standard()
                //TODO Remove endpoint config in production
                //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Regions.DEFAULT_REGION.getName()))
                .withCredentials(credentialsProvider)
                //Comment region for local
                .withRegion(Regions.US_EAST_1)
                .build();
        mapper = new DynamoDBMapper(client);
        CreateTableRequest req = mapper.generateCreateTableRequest(RequestEstimate.class);
        // Table provision throughput is still required since it cannot be specified in your POJO
        req.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
        // Fire off the CreateTableRequest using the low-level client
        TableUtils.createTableIfNotExists(client, req);
        try {
            TableUtils.waitUntilActive(client, RequestEstimate.TABLE);
        } catch (InterruptedException e) {
            logger.warning("Could not wait for table to be active.");
            logger.warning(e.getMessage());
        }
    }

    public long requestEstimate(Request request) {
        RequestEstimate estimate = new RequestEstimate(request);
        RequestEstimate someEstimate = mapper.load(RequestEstimate.class, estimate.getModelName(), estimate.getImageTuple());
        if (someEstimate != null) {
            return someEstimate.getCostPerArea() * request.getImageArea();
        } else {

            Map<String, AttributeValue> values = new HashMap<>();
            values.put(":model_name", new AttributeValue(estimate.getModelName()));
            values.put(":ratio_wrroff", new AttributeValue(String.valueOf(estimate.getRatioWRROFF())));
            values.put(":ratio_wccoff", new AttributeValue(String.valueOf(estimate.getRatioWCCOFF())));
            values.put(":ratio_wrsr", new AttributeValue(String.valueOf(estimate.getRatioWRSR())));
            values.put(":ratio_wcsc", new AttributeValue(String.valueOf(estimate.getRatioWCSC())));
            values.put(":tolerance", new AttributeValue(String.valueOf(TOLERANCE)));

            PaginatedQueryList<RequestEstimate> estimates = mapper.query(RequestEstimate.class,
                    new DynamoDBQueryExpression<RequestEstimate>()
                            .withKeyConditionExpression(
                                    "modelName = :model_name" +
                                    " and ratioWRROFF - :ratio_wrroff <= :tolerance" +
                                    " and ratioWCCOFF - :ratio_wccoff <= :tolerance" +
                                    " and ratioWRSR - :ratio_wrsr <= :tolerance" +
                                    " and ratioWCSC - :ratio_wcsc <= :tolerance" )
                            .withExpressionAttributeValues(values));
            if (estimates.isEmpty()) {
                return 6 * request.getImageArea();
            }
            RequestEstimate theChosenOne = estimates.get(0); //FIXME
            return theChosenOne.getCostPerArea() * request.getImageArea();
        }
    }

    public void storeEstimate(RequestEstimate request) {
        mapper.save(request);
    }
}
