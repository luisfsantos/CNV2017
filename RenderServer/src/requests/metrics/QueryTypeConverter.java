package requests.metrics;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import requests.parser.QueryParser;
import requests.parser.Request;

import java.util.logging.Logger;

/**
 * Created by lads on 14/05/2017.
 */
public class QueryTypeConverter implements DynamoDBTypeConverter<String, Request> {

    private static Logger logger = Logger.getLogger(QueryTypeConverter.class.getName());

    @Override
    public String convert(Request object) {
        Request request = object;
        String requestString = null;
        try {
            if (request != null) {
                requestString = request.getRequestHash();
            }
        }
        catch (Exception e) {
            logger.warning("Couldn't convert request to String.");
            logger.warning(e.getMessage());
        }
        return requestString;
    }

    @Override
    public Request unconvert(String s) {
        Request request = new Request();
        try {
            QueryParser parser = new QueryParser(s);
            request = parser.getRequest();
        }
        catch (Exception e) {
            logger.warning("Couldn't convert String to request.");
            logger.warning("String = " + s);
            logger.warning(e.getMessage());
        }

        return request;
    }
}
