package sg.edu.nus.comp.cs4218.exception;

public class UniqException extends AbstractApplicationException {

    public static final String INVALID_CMD = "Invalid command code.";
    public static final String PROB_UNIQ_FILE = "Problem matching unique from file: ";
    public static final String PROB_UNIQ_STDIN = "Problem matching unique from stdin: ";

    public UniqException(String message) {
        super("uniq: " + message);
    }

    public UniqException(Exception exception, String message) {
        super("sort: " + message);
    }
}
