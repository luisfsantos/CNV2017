package webserver.parser;

import webserver.exception.QueryMissingException;

import java.util.HashMap;

/**
 * Created by lads on 10-04-2017.
 */
public class QueryParser {
    HashMap<String, String> queryMap;
    Query query;

    public QueryParser(String query) throws QueryMissingException {
        queryMap = toMap(query);
        buildQuery();
    }

    private void buildQuery() throws QueryMissingException {
        query = new Query(getSceneColumns(), getSceneRows(), getWindowColumns(), getWindowRows(), getColumnOffset(), getRowOffset(), getSceneFile());
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

    public Query getQuery() {
        return query;
    }

    private String getQueryValue(String key) throws QueryMissingException {
        String value;
        if ((value = queryMap.get(key)) != null ) {
            return value;
        } else throw new QueryMissingException(key);
    }

    private int getSceneColumns() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("sc"));
    }

    private int getSceneRows() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("sr"));
    }

    private int getWindowColumns() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("wc"));
    }

    private int getWindowRows() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("wr"));
    }

    private int getColumnOffset() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("coff"));
    }

    private int getRowOffset() throws QueryMissingException {
        return Integer.parseInt(getQueryValue("roff"));
    }

    private String getSceneFile() throws QueryMissingException {
        return getQueryValue("f");
    }
}
