package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FOLDERS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

public class MkdirApplication implements MkdirInterface {

    /**
     * Runs the mkdir application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     *               if no files are specified.
     * @throws AbstractApplicationException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (args == null || args.length == 0) {
            throw new MkdirException(ERR_NULL_ARGS);
        }

        // Parse argument(s) provided
        MkdirArgsParser parser = new MkdirArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MkdirException(e.getMessage());
        }

        final Boolean isCreateParent = parser.isCreateParent();
        String[] directories = parser.getDirectories().toArray(new String[parser.getDirectories().size()]);

        if (directories.length == 0) {
            throw new MkdirException(ERR_NO_FOLDERS);
        }

        for (String folder : directories) {
            File file = new File(folder);
            if ((isCreateParent && isRootDirectory(file)) ||
                    (!isCreateParent && (isAnyTopLevelFolderMissing(file) || isRootDirectory(file)))) {
                throw new MkdirException(ERR_TOP_LEVEL_MISSING);
            }
            createFolder(folder);
        }
    }

    /**
     * Creates folders specified by the given folder names.
     *
     * @param folderName Array of string of folder names to be created.
     * @throws AbstractApplicationException If folder already exists.
     */
    @Override
    public void createFolder(String... folderName) throws AbstractApplicationException {
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
     * @return true if any top-level folder is missing (including starting with the root symbol). Otherwise, false.
     */
    private boolean isAnyTopLevelFolderMissing(File file) {
        File parentFile = file.getParentFile();
        System.out.println("parentFile: " + parentFile);
        while (parentFile != null) {
            System.out.println("isFileMissing Parent: " + isFileMissing(parentFile));
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

    private boolean isRootDirectory(File file) {
        File parentFile = file.getParentFile();
        while (parentFile != null) {
            if (parentFile.getPath().equals(File.separator)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
        // return directory != null && directory.getPath().equals(File.separator);
    }
}
