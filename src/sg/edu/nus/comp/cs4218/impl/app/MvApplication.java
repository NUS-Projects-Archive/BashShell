package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static sg.edu.nus.comp.cs4218.exception.MvException.PROB_MV_DEST_FILE;
import static sg.edu.nus.comp.cs4218.exception.MvException.PROB_MV_FOLDER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;

public class MvApplication implements MvInterface {

    /**
     * Runs the mv application with the specified arguments
     *
     * @param args   Array of arguments for the application. Each array element is the path to a file
     * @param stdin  An InputStream, not used
     * @param stdout An OutputStream, not used
     * @throws MvException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        // Format: mv [Option] SOURCE TARGET
        //         mv [Option] SOURCE … DIRECTORY

        if (args == null || args.length == 0) {
            throw new MvException(ERR_MISSING_ARG);
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
        final String[] sourceDirectories = parser.getSourceDirectories()
                .toArray(new String[parser.getSourceDirectories().size()]);
        final String destinationDirectory = parser.getDestinationDirectory();

        if (sourceDirectories.length > 1) {
            mvFilesToFolder(isOverwrite, destinationDirectory, sourceDirectories);
        } else {
            mvSrcFileToDestFile(isOverwrite, sourceDirectories[0], destinationDirectory);
        }
    }

    /**
     * Moves a source file to a destination file with the option to overwrite
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param srcFile     String representing the path to the source file
     * @param destFile    String representing the path to the destination file
     * @return null.
     * @throws MvException
     */
    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        Path srcPath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);

        // Ensure that source file exist
        if (!Files.exists(srcPath)) {
            throw new MvException(PROB_MV_DEST_FILE + ERR_FILE_NOT_FOUND);
        }

        // Ensure that files have the required permissions
        if (!Files.isReadable(srcPath) || (Files.exists(destPath) && !Files.isWritable(destPath))) {
            throw new MvException(PROB_MV_DEST_FILE + ERR_NO_PERM);
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
            throw new MvException(PROB_MV_DEST_FILE + ERR_IO_EXCEPTION, e);
        }

        return null;
    }

    /**
     * Moves multiple source files to a destination folder with option to overwrite
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param destFolder  String representing the path to the destination folder
     * @param fileName    Array of String representing the file names
     * @return null.
     * @throws MvException
     */
    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        Path destFolderPath = IOUtils.resolveFilePath(destFolder);

        // Ensure that destination folder exist
        if (!Files.exists(destFolderPath)) {
            throw new MvException(PROB_MV_FOLDER + ERR_FILE_NOT_FOUND);
        }

        // Ensure that destination folder is a directory
        if (!Files.isDirectory(destFolderPath)) {
            throw new MvException(PROB_MV_FOLDER + ERR_IS_DIR);
        }

        // Ensure that destination folder have the required write permission
        if (!Files.isWritable(destFolderPath)) {
            throw new MvException(PROB_MV_FOLDER + ERR_NO_PERM);
        }

        for (String srcFile : fileName) {
            Path srcPath = IOUtils.resolveFilePath(srcFile);
            Path destPath = destFolderPath.resolve(srcPath.getFileName());

            // Ensure that source file exist
            if (!Files.exists(srcPath)) {
                throw new MvException(PROB_MV_FOLDER + ERR_FILE_NOT_FOUND);
            }

            // Ensure that source file have the required read permission
            if (!Files.isReadable(srcPath)) {
                throw new MvException(PROB_MV_FOLDER + ERR_NO_PERM);
            }

            try {
                // Overwrite an existing file only if either it do not exist or overwrite flag is given
                if (!Files.exists(destPath) || isOverwrite) {
                    Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new MvException(PROB_MV_FOLDER + ERR_IO_EXCEPTION, e);
            }
        }

        return null;
    }
}
