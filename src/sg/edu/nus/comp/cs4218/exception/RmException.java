package sg.edu.nus.comp.cs4218.exception;

import java.util.List;

public class RmException extends AbstractApplicationException {

    private static final long serialVersionUID = 6616752571518808461L;

    public RmException(String message) {
        super("rm: " + message);
    }

    public RmException(String message, Throwable throwable) {
        super("rm: " + message, throwable);
    }

    public RmException(List<RmException> exceptions) {
        super(exceptions);
    }

}