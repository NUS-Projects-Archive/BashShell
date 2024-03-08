package sg.edu.nus.comp.cs4218.exception;

import java.util.List;

public class MkdirException extends AbstractApplicationException {

    private static final long serialVersionUID = -7005801205007805286L;

    public MkdirException(String message) {
        super("mkdir: " + message);
    }

    public MkdirException(String message, Throwable cause) {
        super("mkdir: " + message, cause);
    }

    public MkdirException(List<MkdirException> exceptions) {
        super(exceptions);
    }
}
