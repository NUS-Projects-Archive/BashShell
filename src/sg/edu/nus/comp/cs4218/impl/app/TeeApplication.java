package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_GENERAL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public class TeeApplication implements TeeInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws TeeException {
        if (args == null) {
            throw new TeeException(ERR_NO_ARGS);
        }
        if (stdin == null) {
            throw new TeeException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new TeeException(ERR_NO_OSTREAM);
        }

        TeeArgsParser parser = new TeeArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage(), e);
        }

        boolean isAppend = parser.isAppend();
        String[] files = CollectionsUtils.listToArray(parser.getFileNames());

        String result = teeFromStdin(isAppend, stdin, files);
        try {
            stdout.write(result.getBytes());
        } catch (Exception e) {
            throw new TeeException(ERR_WRITE_STREAM, e);
        }
    }

    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws TeeException {
        if (stdin == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }
        if (fileName == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }
        List<String> data = null;

        try {
            data = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
        String dataToString = String.join(STRING_NEWLINE, data) + STRING_NEWLINE;
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            String absolutePath = null;
            if (node.exists()) {
                absolutePath = node.getAbsolutePath();
            } else {
                try {
                    absolutePath = createEmptyFile(file);
                } catch (Exception e) {
                    throw new TeeException(e.getMessage(), e);
                }
            }
            if (node.isDirectory()) {
                return String.format("tee: %s: Is a directory", file) + STRING_NEWLINE + dataToString;
            }
            writeToFile(isAppend, dataToString, absolutePath);
        }

        return dataToString;
    }

    public String createEmptyFile(String file) throws TeeException {
        Path path = Paths.get(file).normalize();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory).resolve(path);
        }
        File newFile = new File(path.toString());

        try {
            if (newFile.createNewFile()) {
                return path.toString();
            } else {
                throw new TeeException(ERR_GENERAL);
            }
        } catch (IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
    }

    public void writeToFile(Boolean isAppend, String content, String filePath) throws TeeException {
        try {
            Path path = Paths.get(filePath);
            if (isAppend) {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } else {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
    }
}
