package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    // private static final long serialVersionUID = ???; //FIXME

    public MvException(String message) {
        super("mv: " + message);
    }
}
