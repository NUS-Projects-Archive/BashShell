package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

/**
 * The rm command attempts to remove the non-directory type files specified
 * on the command line.
 *
 * <p>
 * <b>Command format:</b> <code>rm [Option] FILES...</code>
 * </p>
 */
public class RmApplication implements RmInterface {

    /**
     * Runs the rm application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a file
     * @param stdin  An InputStream, not used
     * @param stdout An OutputStream, not used
     * @throws RmException If the arguments are null or an empty array,
     *                     or if there is an issue deleting files
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        if (args == null || args.length == 0) {
            throw new RmException(ERR_NULL_ARGS);
        }

        // Parse argument(s) provided
        final RmArgsParser parser = new RmArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e.getMessage(), e);
        }

        final boolean isEmptyFolder = parser.isEmptyDirectory();
        final boolean isRecursive = parser.isRecursive();
        final String[] filesToRemove = listToArray(parser.getFiles());

        if (filesToRemove.length == 0) {
            throw new RmException(ERR_NO_ARGS);
        } else {
            remove(isEmptyFolder, isRecursive, filesToRemove);
        }
    }

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {
        final Path currentDirectory = Paths.get(Environment.currentDirectory);
        List<RmException> errorList = new ArrayList<>();

        for (String file : fileName) {
            try {
                // File is relative path, need to change to absolute path
                File fileToDelete = new File(currentDirectory.resolve(file).toString());

                if (!fileToDelete.exists()) {
                    throw new RmException(String.format("cannot remove '%s': No such file or directory", file));
                }

                // Is a folder -> check flags
                if (fileToDelete.isDirectory()) {
                    String[] fileContents = fileToDelete.list();
                    if (isRecursive) {
                        Environment.currentDirectory = fileToDelete.getAbsolutePath();
                        if (fileContents != null && fileContents.length > 0) {
                            remove(isEmptyFolder, true, fileContents); // recursively delete contents
                        }
                        Environment.currentDirectory = currentDirectory.toString();

                        deleteFile(fileToDelete); // Contents deleted, now delete folder
                    } else if (isEmptyFolder) {
                        if (fileContents == null || fileContents.length == 0) {
                            deleteFile(fileToDelete);
                        } else {
                            throw new RmException(String.format("cannot remove '%s': Directory not empty", file));
                        }
                    } else {
                        throw new RmException(String.format("cannot remove '%s': Is a directory", file));
                    }
                }

                // Is a file -> delete it
                if (fileToDelete.isFile()) {
                    deleteFile(fileToDelete);
                }

            } catch (RmException exception) {
                errorList.add(exception);
            }
        }

        if (!errorList.isEmpty()) {
            throw new RmException(errorList);
        }
    }

    /**
     * Deletes specified file.
     *
     * @param file File to delete
     * @throws RmException if it failed to delete file
     */
    private void deleteFile(final File file) throws RmException {
        boolean isSuccess = file.delete();
        if (!isSuccess) {
            throw new RmException("Failed to delete " + file);
        }
    }
}
