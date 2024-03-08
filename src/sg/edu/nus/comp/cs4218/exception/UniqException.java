package sg.edu.nus.comp.cs4218.exception;

public class UniqException extends AbstractApplicationException {

    private static final long serialVersionUID = -5428580805384267184L;
    public static final String PROB_UNIQ_FILE = "Problem matching unique from file: ";
    public static final String PROB_UNIQ_STDIN = "Problem matching unique from stdin: ";
    public static final String COUNT_ALL_DUP_ERR = "printing all duplicated lines and repeat " +
            "counts is meaningless";

    public UniqException(final String message) {
        super("uniq: " + message);
    }

    public UniqException(String message, Throwable cause) {
        super("uniq: " + message, cause);
    }
}
