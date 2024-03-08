package sg.edu.nus.comp.cs4218.exception;

import java.util.List;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = -2890717950640280147L;

    public MvException(String message) {
        super("mv: " + message);
    }

    public MvException(String message, Throwable cause) {
        super("mv: " + message, cause);
    }

    public MvException(List<MvException> exceptions) {
        super(exceptions);
    }
}
