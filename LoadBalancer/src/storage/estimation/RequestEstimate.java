package storage.estimation;

import com.amazonaws.Request;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import requests.metrics.QueryTypeConverter;

/**
 * Created by lads on 19/05/2017.
 */
@DynamoDBTable(tableName = RequestEstimate.TABLE)
public class RequestEstimate {
    public static final String TABLE = "EstimatesStore";
    private String modelName;
    private String imageTuple;
    private Request request;
    private long costPerArea;
    private double ratioWRROFF;
    private double ratioWCCOFF;
    private double ratioWCSC;
    private double ratioWRSR;


    public RequestEstimate() {

    }

    public RequestEstimate(Request request, long costPerArea) {
        this.request = request;
    }

    public RequestEstimate(Request request) {
        this(request, 0);
    }

    @DynamoDBHashKey(attributeName = "modelName")
    public String getModelName() {
        return modelName;
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

    @DynamoDBAttribute(attributeName = "costPerArea")
    public long getCostPerArea() {
        return costPerArea;
    }

    public void setCostPerArea(long costPerArea) {
        this.costPerArea = costPerArea;
    }

    public double getRatioWRROFF() {
        return ratioWRROFF;
    }

    public void setRatioWRROFF(double ratioWRROFF) {
        this.ratioWRROFF = ratioWRROFF;
    }

    public String getImageTuple() {
        return makeImageTuple();
    }
    @DynamoDBRangeKey(attributeName = "imageTuple")
    public void setImageTuple(String imageTuple) {
        this.imageTuple = imageTuple;
    }

    public double getRatioWCCOFF() {
        return ratioWCCOFF;
    }

    public void setRatioWCCOFF(double ratioWCCOFF) {
        this.ratioWCCOFF = ratioWCCOFF;
    }

    public double getRatioWCSC() {
        return ratioWCSC;
    }

    public void setRatioWCSC(double ratioWCSC) {
        this.ratioWCSC = ratioWCSC;
    }

    public double getRatioWRSR() {
        return ratioWRSR;
    }

    public void setRatioWRSR(double ratioWRSR) {
        this.ratioWRSR = ratioWRSR;
    }

    public String makeImageTuple() {
        return "(" + ")";
    }

}
