package sg.edu.nus.comp.cs4218.exception;

/**
 * This exception is thrown only when the directory passed as an argument is invalid.
 * It is considered invalid if it does not exist.
 */
public class InvalidDirectoryLsException extends LsException {

    private static final long serialVersionUID = -6109499076919173646L;

    public InvalidDirectoryLsException(String directory) {
        super(String.format("cannot access '%s': No such file or directory", directory));
    }

    public InvalidDirectoryLsException(String directory, Throwable cause) {
        super(String.format("cannot access '%s': No such file or directory", directory, cause));
    }
}
