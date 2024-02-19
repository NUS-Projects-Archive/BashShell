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

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

public class MvApplication implements MvInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        // Format: mv [Option] SOURCE TARGET
        //         mv [Option] SOURCE … DIRECTORY

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
            throw new MvException(e.getMessage());
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

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        Path srcPath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);

        // Ensure that source file exist
        if (!Files.exists(srcPath)) {
            throw new MvException(ERR_FILE_NOT_FOUND);
        }

        // Ensure that destination is not a directory
        if (Files.isDirectory(destPath)) {
            throw new MvException(ERR_IS_DIR);
        }

        // Ensure that the files have the required permissions
        if (!Files.isReadable(srcPath) || (Files.exists(destPath) && !Files.isWritable(destPath))) {
            throw new MvException(ERR_NO_PERM);
        }

        try {
            // Overwrite an existing file only if flag is given or file do not exist
            if (!Files.exists(destPath) || isOverwrite) {
                Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new MvException(ERR_IO_EXCEPTION + e.getMessage());
        }

        return null;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        return null;
    }
}
