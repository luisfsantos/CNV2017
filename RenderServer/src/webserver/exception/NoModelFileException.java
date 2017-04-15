package webserver.exception;

/**
 * Created by lads on 10-04-2017.
 */
public class NoModelFileException extends Exception {

    public NoModelFileException(String f) {
        super("Cannot find the model file <" + f + ">");
    }
}
