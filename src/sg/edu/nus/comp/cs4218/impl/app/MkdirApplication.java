package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FOLDERS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;

/**
 * The mkdir command creates new folders, if they do not already exist.
 *
 * <p>
 * <b>Command format:</b> <code>mkdir [Option] DIRECTORIES...</code>
 * </p>
 */
public class MkdirApplication implements MkdirInterface {

    /**
     * Runs the mkdir application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a file
     * @param stdin  An InputStream, not used
     * @param stdout An OutputStream, not used
     * @throws MkdirException If the arguments are null or an empty array,
     *                        or if there is an issue parsing or creating directories
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MkdirException {
        if (args == null || args.length == 0) {
            throw new MkdirException(ERR_NULL_ARGS);
        }

        // Parse argument(s) provided
        final MkdirArgsParser parser = new MkdirArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MkdirException(e.getMessage(), e);
        }

        final Boolean isCreateParent = parser.isCreateParent();
        final String[] directories = listToArray(parser.getDirectories());

        if (directories.length == 0) {
            throw new MkdirException(ERR_NO_FOLDERS);
        }

        List<MkdirException> errorList = new ArrayList<>();
        for (String folder : directories) {
            try {
                String folderAbsPath = getAbsolutePath(folder);
                File file = new File(folderAbsPath);

                if ((!isCreateParent && isAnyTopLevelFolderMissing(file))) {
                    throw new MkdirException(String.format("cannot create directory '%s': %s", folder, ERR_TOP_LEVEL_MISSING));
                }
                if (!isCreateParent && file.exists()) {
                    throw new MkdirException(String.format("cannot create directory '%s': %s", folder, ERR_FILE_EXISTS));
                }
                createFolder(folder);
            } catch (MkdirException exception) {
                errorList.add(exception);
            }
        }

        if (!errorList.isEmpty()) {
            throw new MkdirException(errorList);
        }
    }

    /**
     * Creates folders specified by the given folder names.
     *
     * @param folderName Array of string of folder names to be created
     * @throws MkdirException If folder already exists
     */
    @Override
    public void createFolder(String... folderName) throws MkdirException {
        if (folderName == null || folderName.length == 0) {
            throw new MkdirException(ERR_NULL_ARGS);
        }

        for (String folder : folderName) {
            if (folder.isEmpty()) {
                throw new MkdirException(ERR_NO_FOLDERS);
            }
            String folderAbsPath = getAbsolutePath(folder);
            File file = new File(folderAbsPath);
            file.mkdirs();
        }
    }

    /**
     * Checks if any top-level folder in the parent hierarchy of the given file is missing.
     *
     * @param file The file to start the check from
     * @return true if any top-level folder is missing; Otherwise, false
     */
    private boolean isAnyTopLevelFolderMissing(File file) {
        File parentFile = file.getParentFile();
        while (parentFile != null) {
            if (isFileMissing(parentFile)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }

    private boolean isFileMissing(File file) {
        return !file.exists();
    }

    /**
     * Returns the absolute path for the given folder name.
     *
     * @param folderName The folder name
     * @return The absolute path
     */
    private String getAbsolutePath(String folderName) {
        Path currentDirectory = Paths.get(Environment.currentDirectory);
        return currentDirectory.resolve(folderName).toString();
    }
}
