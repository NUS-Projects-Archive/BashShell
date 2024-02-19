package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    // private static final long serialVersionUID = ???; //FIXME
    public static final String PROB_MV_DEST_FILE = "Problem move to destination file: ";
    public static final String PROB_MV_FOLDER = "Problem move to folder: ";

    public MvException(String message) {
        super("mv: " + message);
    }
}
