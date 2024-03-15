package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The mv command move files or directories from one place to another.
 * By default, it will overwrite an existing file.
 *
 * <p>
 * <b>Command format:</b> <br>
 * <code>mv [Option] SOURCE TARGET</code> <br>
 * <code>mv [Option] SOURCE ... DIRECTORY</code>
 * </p>
 */
public class MvApplication implements MvInterface {

    /**
     * Runs the mv application with the specified arguments
     *
     * @param args   Array of arguments for the application. Each array element is the path to a file
     * @param stdin  An InputStream, not used
     * @param stdout An OutputStream, not used
     * @throws MvException If the arguments are null or insufficient,
     *                     or if there is an issue parsing the move operation
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (args == null || args.length == 0) {
            throw new MvException(ERR_NULL_ARGS);
        }
        if (args.length < 2) {
            throw new MvException(ERR_NO_ARGS);
        }

        // Parse argument(s) provided
        final MvArgsParser parser = new MvArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e.getMessage(), e);
        }

        final Boolean isOverwrite = parser.isOverwrite();
        final String[] srcDirectories = listToArray(parser.getSourceDirectories());
        final String destDirectory = parser.getDestinationDirectory();

        if (srcDirectories.length == 0) {
            throw new MvException(ERR_NO_ARGS);
        } else if (srcDirectories.length == 1) {
            mvSrcFileToDestFile(isOverwrite, srcDirectories[0], destDirectory);
        } else {
            mvFilesToFolder(isOverwrite, destDirectory, srcDirectories);
        }
    }

    /**
     * Moves a source file to a destination file with the option to overwrite
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param srcFile     String representing the path to the source file
     * @param destFile    String representing the path to the destination file
     * @return Null
     * @throws MvException If moving encounters issues, e.g., file not found, permission errors, or I/O exception
     */
    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        Path srcPath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);

        if (!Files.exists(srcPath)) {
            throw new MvException(formatFileExceptionMsg(srcFile, ERR_FILE_NOT_FOUND));
        }

        if (!Files.isReadable(srcPath)) {
            throw new MvException(formatFileExceptionMsg(srcFile, ERR_READING_FILE));
        }

        if (!Files.isWritable(srcPath)) {
            throw new MvException(formatFileExceptionMsg(srcFile, ERR_NO_PERM));
        }

        if (Files.exists(destPath) && !Files.isWritable(destPath)) {
            throw new MvException(formatFileExceptionMsg(destFile, ERR_NO_PERM));
        }

        // Append file name to destination directory if destination is a directory
        if (Files.isDirectory(destPath)) {
            destPath = destPath.resolve(srcPath.getFileName());
        }

        try {
            // Overwrite an existing file only if either it do not exist or overwrite flag is given
            if (!Files.exists(destPath) || isOverwrite) {
                Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new MvException(ERR_IO_EXCEPTION, e);
        }

        return null;
    }

    /**
     * Moves multiple source files to a destination folder with option to overwrite
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param destFolder  String representing the path to the destination folder
     * @param fileName    Array of String representing the file names
     * @return Null
     * @throws MvException If moving encounters issues, e.g., file not found, permission errors, or I/O exception
     */
    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        Path destFolderPath = IOUtils.resolveFilePath(destFolder);

        if (!Files.exists(destFolderPath)) {
            throw new MvException(formatFileExceptionMsg(destFolder, ERR_FILE_NOT_FOUND));
        }

        if (!Files.isDirectory(destFolderPath)) {
            throw new MvException(formatFileExceptionMsg(destFolder, ERR_IS_NOT_DIR));
        }

        if (!Files.isWritable(destFolderPath)) {
            throw new MvException(formatFileExceptionMsg(destFolder, ERR_NO_PERM));
        }

        List<MvException> errorList = new ArrayList<>();
        for (String srcFile : fileName) {
            try {
                Path srcPath = IOUtils.resolveFilePath(srcFile);
                Path destPath = destFolderPath.resolve(srcPath.getFileName());

                if (!Files.exists(srcPath)) {
                    throw new MvException(formatFileExceptionMsg(srcFile, ERR_FILE_NOT_FOUND));
                }

                if (!Files.isReadable(srcPath)) {
                    throw new MvException(formatFileExceptionMsg(srcFile, ERR_READING_FILE));
                }

                try {
                    // Overwrite an existing file only if either it do not exist or overwrite flag is given
                    if (!Files.exists(destPath) || isOverwrite) {
                        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new MvException(ERR_IO_EXCEPTION, e);
                }
            } catch (MvException exception) {
                errorList.add(exception);
            }
        }

        if (!errorList.isEmpty()) {
            throw new MvException(errorList);
        }

        return null;
    }

    private String formatFileExceptionMsg(String file, String error) {
        return String.format("'%s': %s", file, error);
    }
}
