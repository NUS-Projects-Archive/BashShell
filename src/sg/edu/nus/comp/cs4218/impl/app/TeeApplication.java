package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.TeeApplicationHelper.createEmptyFile;
import static sg.edu.nus.comp.cs4218.impl.app.helper.TeeApplicationHelper.writeToFile;
import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
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
import java.util.List;

import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The tee utility takes in a standard input and writes to standard output. If a file is specified,
 * it either overwrites the file with the standard input, or appends the input to the file.
 *
 * <p>
 * <b>Command format:</b> <code>tee [Options] [FILES]</code>
 * </p>
 */
public class TeeApplication implements TeeInterface {

    /**
     * Runs the tee application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a file or a flag.
     * @param stdin  An InputStream. The input to be used for this command is read from this InputStream.
     * @param stdout An OutputStream. The output of the command is written to this Output Stream.
     * @throws TeeException
     */
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

        final TeeArgsParser parser = new TeeArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage(), e);
        }

        final boolean isAppend = parser.isAppend();
        final String[] files = listToArray(parser.getFileNames());

        String result = teeFromStdin(isAppend, stdin, files);
        try {
            stdout.write(result.getBytes());
        } catch (IOException e) {
            throw new TeeException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string containing standard input.
     *
     * @param isAppend Boolean option to append the standard input to the contents of the input files
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return A formatted string of the given standard input.
     * @throws TeeException
     */
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
                absolutePath = createEmptyFile(file);
            }
            if (node.isDirectory()) {
                return String.format("tee: %s: Is a directory", file) + STRING_NEWLINE + dataToString;
            }
            writeToFile(isAppend, dataToString, absolutePath);
        }

        return dataToString;
    }
}
