package webserver.parser;

import java.util.HashMap;

/**
 * Created by lads on 10-04-2017.
 */
public class QueryParser {
    String query;

    public QueryParser(String query) {
        this.query = query;
    }

    public HashMap<String, String> toMap() {
        HashMap<String, String> parameterValues = new HashMap<>();
        String[] queries = query.split("&");
        for (String p : queries) {
            String[] paramVal = p.split("=");
            if (paramVal.length <= 1) {
                parameterValues.put(paramVal[0], "");
            } else {
                parameterValues.put(paramVal[0], paramVal[1]);
            }
        }
        return parameterValues;
    }
}
