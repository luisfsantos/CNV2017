package requests.exception;

/**
 * Created by lads on 15/04/2017.
 */
public class QueryMissingException extends Exception {
    public QueryMissingException(String f) {
        super("the query " + f + " is missing please use the format \n" +
                "r.html?f=<model-filename>&sc=<scene-columns>&sr=<scene-rows>&wc=<window-columns>&wr=<window-rows>&coff=<column-offset>&roff=<row-offset>\n");
    }
}
