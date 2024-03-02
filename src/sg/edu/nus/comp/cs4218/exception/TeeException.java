package sg.edu.nus.comp.cs4218.exception;

public class TeeException extends AbstractApplicationException {

    private static final long serialVersionUID = -2165821652621360327L;

    public TeeException(String message) {
        super("tee: " + message);
    }

    public TeeException(String message, Throwable cause) {
        super("tee: " + message, cause);
    }
}
