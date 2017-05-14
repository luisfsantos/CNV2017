package requests.parser;

import requests.exception.QueryMissingException;

import java.util.HashMap;

/**
 * Created by lads on 10-04-2017.
 */
public class QueryParser {
    HashMap<String, String> queryMap;
    Request request;

    public QueryParser(String query) throws QueryMissingException {
        queryMap = toMap(query);
        buildQuery();
    }

    private void buildQuery() throws QueryMissingException {
        request = new Request(getRequestID(), getSceneColumns(), getSceneRows(), getWindowColumns(), getWindowRows(), getColumnOffset(), getRowOffset(), getSceneFile());
    }

    public static HashMap<String, String> toMap(String queryString) {
        HashMap<String, String> parameterValues = new HashMap<>();
        if (queryString != null) {
            String[] queries = queryString.split("&");
            for (String p : queries) {
                String[] paramVal = p.split("=");
                if (paramVal.length <= 1) {
                    parameterValues.put(paramVal[0], "");
                } else {
                    parameterValues.put(paramVal[0], paramVal[1]);
                }
            }
        }
        return parameterValues;
    }

    public Request getRequest() {
        return request;
    }

    private String getQueryValue(String key) throws QueryMissingException {
        String value;
        if ((value = queryMap.get(key)) != null ) {
            return value;
        } else throw new QueryMissingException(key);
    }

    public int getSceneColumns() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("sc"));
    }

    public int getSceneRows() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("sr"));
    }

    public int getWindowColumns() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("wc"));
    }

    public int getWindowRows() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("wr"));
    }

    public int getColumnOffset() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("coff"));
    }

    public int getRowOffset() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("roff"));
    }

    public String getSceneFile() throws QueryMissingException {
        return getQueryValue("f");
    }

    public String getRequestID() {
        try {
            return getQueryValue("id");
        } catch (QueryMissingException e) {
            return null;
        }
    }
}
