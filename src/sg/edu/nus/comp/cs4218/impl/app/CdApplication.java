package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * The mkdir command changes the current working folder.
 *
 * <p>
 * <b>Command format:</b> <code>cd PATH</code>
 * </p>
 */
public class CdApplication implements CdInterface {

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one arg. (cd without args is not supported)
     *
     * @param args   Array of arguments for the application
     * @param stdin  An InputStream, not used
     * @param stdout An OutputStream, not used
     * @throws CdException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CdException {
        if (stdin == null || stdout == null) {
            throw new CdException(ERR_NULL_STREAMS);
        }

        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        } else if (args.length > 1) {
            throw new CdException(ERR_TOO_MANY_ARGS);
        } else if (args.length == 0) {
            changeToDirectory("");
        } else if (args.length == 1) {
            changeToDirectory(args[0]);
        }
    }

    /**
     * Changes the environment context to a different directory.
     *
     * @param path String of the path to a directory
     * @throws CdException
     */
    @Override
    public void changeToDirectory(String path) throws CdException {
        Environment.currentDirectory = getNormalizedAbsolutePath(path);
    }

    /**
     * Returns the normalized absolute path for the given path string.
     *
     * @param pathStr The input path string to be normalized
     * @return The normalized absolute path as a string
     * @throws CdException
     */
    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        if (StringUtils.isBlank(pathStr)) {
            return System.getProperty("user.dir");
        }

        Path path = new File(pathStr).toPath();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory, pathStr);
        }

        if (Files.isDirectory(path) && !Files.isExecutable(path)) {
            // Path is a directory but cannot be executed (i.e. cannot cd into)
            throw new CdException(String.format("%s: %s", pathStr, ERR_NO_PERM));
        }

        if (!Files.exists(path)) {
            throw new CdException(String.format("%s: %s", pathStr, ERR_FILE_NOT_FOUND));
        }

        if (!Files.isDirectory(path)) {
            throw new CdException(String.format("%s: %s", pathStr, ERR_IS_NOT_DIR));
        }

        return path.normalize().toString();
    }
}
