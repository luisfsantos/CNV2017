package requests.metrics;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import requests.parser.Request;

/**
 * Created by lads on 14/05/2017.
 */
@DynamoDBTable(tableName = RequestMetrics.TABLE)
public class RequestMetrics {
    public static final String TABLE = "MetricsStore";
    private String requestID;
    private Request request;
    private long finalMethods;
    private long estimatedMethods;
    private long currentMethods;

    public RequestMetrics() {

    }

    public RequestMetrics(Request request) {
        this.requestID = request.getRequestID();
        this.request = request;
    }

    @DynamoDBHashKey(attributeName = "requestID")
    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    @DynamoDBTypeConverted(converter = QueryTypeConverter.class)
    @DynamoDBAttribute(attributeName = "request")
    public Request getRequest() {
        return request;
    }
    @DynamoDBAttribute(attributeName = "request")
    public void setRequest(Request request) {
        this.request = request;
    }

    @DynamoDBAttribute(attributeName = "finalMethods")
    public long getFinalMethods() {
        return finalMethods;
    }

    public void setFinalMethods(long finalMethods) {
        this.finalMethods = finalMethods;
    }

    @DynamoDBAttribute(attributeName = "estimatedMethods")
    public long getEstimatedMethods() {
        return estimatedMethods;
    }

    public void setEstimatedMethods(long estimatedMethods) {
        this.estimatedMethods = estimatedMethods;
    }

    @DynamoDBAttribute(attributeName = "currentMethods")
    public long getCurrentMethods() {
        return currentMethods;
    }

    public void setCurrentMethods(long currentMethods) {
        this.currentMethods = currentMethods;
    }
}
