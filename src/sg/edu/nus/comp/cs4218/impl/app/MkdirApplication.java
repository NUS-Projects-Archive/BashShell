package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FOLDERS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;

public class MkdirApplication implements MkdirInterface {

    /**
     * Runs the mkdir application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a file.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws MkdirException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MkdirException {
        if (args == null || args.length == 0) {
            throw new MkdirException(ERR_NULL_ARGS);
        }

        // Parse argument(s) provided
        MkdirArgsParser parser = new MkdirArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MkdirException(e.getMessage(), e);
        }

        final Boolean isCreateParent = parser.isCreateParent();
        String[] directories = parser.getDirectories().toArray(new String[parser.getDirectories().size()]);

        if (directories.length == 0) {
            throw new MkdirException(ERR_NO_FOLDERS);
        }

        for (String folder : directories) {
            File file = new File(folder);
            if ((!isCreateParent && isAnyTopLevelFolderMissing(file)) || isStartFromRoot(file)) {
                throw new MkdirException(ERR_TOP_LEVEL_MISSING);
            }
            createFolder(folder);
        }
    }

    /**
     * Creates folders specified by the given folder names.
     *
     * @param folderName Array of string of folder names to be created.
     * @throws MkdirException If folder already exists.
     */
    @Override
    public void createFolder(String... folderName) throws MkdirException {
        for (String folder : folderName) {
            File file = new File(folder);

            if (file.exists()) {
                throw new MkdirException(ERR_FILE_EXISTS);
            }

            if (!file.mkdirs()) {
                throw new MkdirException("Failed to create folder: " + file);
            }
        }
    }

    /**
     * Checks if any top-level folder in the parent hierarchy of the given file is missing.
     *
     * @param file The file to start the check from.
     * @return true if any top-level folder is missing; Otherwise, false.
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

    /**
     * Checks if the given file starts from the root directory.
     *
     * @param file The file to check.
     * @return true if the file paths starts with the root symbol ("/"); Otherwise, false.
     */
    private boolean isStartFromRoot(File file) {
        String filePath = file.getPath();
        return filePath.startsWith(File.separator);
    }

    private boolean isFileMissing(File file) {
        return !file.exists();
    }
}
