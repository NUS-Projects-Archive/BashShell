package sg.edu.nus.comp.cs4218.exception;

/**
 * This exception is thrown only when the directory passed as an argument is
 * valid but the user does not have permission to access it.
 */
public class DirectoryAccessDeniedLsException extends LsException {
    private static final long serialVersionUID = -7253356637881313851L;

    public DirectoryAccessDeniedLsException(String directory) {
        super(String.format("cannot open directory '%s': Permission denied", directory));
    }
}
