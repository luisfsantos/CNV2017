package storage.estimation;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import requests.metrics.QueryTypeConverter;
import requests.parser.Request;

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
        this.costPerArea = costPerArea;
        init();
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

    @DynamoDBAttribute(attributeName = "costPerArea")
    public void setCostPerArea(long costPerArea) {
        this.costPerArea = costPerArea;
    }

    @DynamoDBAttribute(attributeName = "ratioWRROFF")
    public double getRatioWRROFF() {
        return ratioWRROFF;
    }

    @DynamoDBAttribute(attributeName = "ratioWRROFF")
    public void setRatioWRROFF(double ratioWRROFF) {
        this.ratioWRROFF = ratioWRROFF;
    }

    @DynamoDBRangeKey(attributeName = "imageTuple")
    public String getImageTuple() {
        return makeImageTuple();
    }

    @DynamoDBAttribute(attributeName = "imageTuple")
    public void setImageTuple(String imageTuple) {
        this.imageTuple = imageTuple;
    }

    @DynamoDBAttribute(attributeName = "ratioWCCOFF")
    public double getRatioWCCOFF() {
        return ratioWCCOFF;
    }

    @DynamoDBAttribute(attributeName = "ratioWCCOFF")
    public void setRatioWCCOFF(double ratioWCCOFF) {
        this.ratioWCCOFF = ratioWCCOFF;
    }

    @DynamoDBAttribute(attributeName = "ratioWCSC")
    public double getRatioWCSC() {
        return ratioWCSC;
    }

    @DynamoDBAttribute(attributeName = "ratioWCSC")
    public void setRatioWCSC(double ratioWCSC) {
        this.ratioWCSC = ratioWCSC;
    }

    @DynamoDBAttribute(attributeName = "ratioWRSR")
    public double getRatioWRSR() {
        return ratioWRSR;
    }

    @DynamoDBAttribute(attributeName = "ratioWRSR")
    public void setRatioWRSR(double ratioWRSR) {
        this.ratioWRSR = ratioWRSR;
    }

    public String makeImageTuple() {
        return "(" +
                this.ratioWRROFF + ", " +
                this.ratioWCCOFF + ", " +
                this.ratioWRSR + ", " +
                this.ratioWCSC + ")";
    }

    public void init() {

        // ratio WR/ROFF
        if (request.getWindowRows() == 0 || request.getRowOffset() == 0) {
            this.setRatioWRROFF(0.0);
        } else {
            this.setRatioWRROFF(
                    request.getWindowRows() / request.getRowOffset()
            );
        }

        // ratio WC/COFF
        if (request.getWindowColumns() == 0 || request.getColumnOffset() == 0) {
            this.setRatioWCCOFF(0.0);
        } else {
            this.setRatioWCCOFF(
                    request.getWindowColumns() / request.getColumnOffset()
            );
        }

        // ratio WR/SR
        if (request.getWindowRows() == 0 || request.getSceneRows() == 0) {
            this.setRatioWRSR(0.0);
        } else {
            this.setRatioWRSR(
                    request.getWindowRows() / request.getRowOffset()
            );
        }

        // ratio WC/SC
        if (request.getWindowColumns() == 0 || request.getSceneColumns() == 0) {
            this.setRatioWCSC(0.0);
        } else {
            this.setRatioWCSC(
                    request.getWindowColumns() / request.getSceneRows()
            );
        }
    }

}
