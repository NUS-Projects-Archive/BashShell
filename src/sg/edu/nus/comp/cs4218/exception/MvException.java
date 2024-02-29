package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = -2890717950640280147L;
    public static final String PROB_MV_DEST_FILE = "Problem move to destination file: ";
    public static final String PROB_MV_FOLDER = "Problem move to folder: ";

    public MvException(String message) {
        super("mv: " + message);
    }

    public MvException(String message, Throwable cause) {
        super(message, cause);
    }
}
