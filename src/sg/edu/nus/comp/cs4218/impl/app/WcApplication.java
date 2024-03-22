package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.WcApplicationHelper.formatCount;
import static sg.edu.nus.comp.cs4218.impl.app.helper.WcApplicationHelper.getCountReport;
import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The wc utility displays the number of lines, words, and bytes contained in each
 * input file, or standard input (if no file is specified) to the standard output.
 *
 * <p>
 * <b>Command format:</b> <code>wc [Options] [FILES]<code>
 * </p>
 */
public class WcApplication implements WcInterface {

    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;
    private final long[] totals = {0, 0, 0}; // represents totalLines, totalWords, totalBytes

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws WcException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws WcException {
        if (stdout == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }

        final WcArgsParser parser = new WcArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException | NullPointerException e) {
            throw new WcException(e.getMessage(), e);
        }
        final Boolean isStdinOnly = parser.isStdinOnly();
        final Boolean isByteCount = parser.isByteCount();
        final Boolean isLineCount = parser.isLineCount();
        final Boolean isWordCount = parser.isWordCount();
        final String[] files = listToArray(parser.getFileNames());

        StringBuilder output = new StringBuilder();
        if (isStdinOnly) {
            output.append(countFromStdin(isByteCount, isLineCount, isWordCount, stdin));
        } else {
            output.append(countFromFileAndStdin(isByteCount, isLineCount, isWordCount, stdin, files));
        }

        try {
            if (output.length() != 0) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new WcException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files.
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names (not including "-" for reading from stdin)
     * @return A formatted string representing the counts
     * @throws WcException
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 String... fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(ERR_NULL_ARGS);
        }

        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new WcException(String.format("'%s': %s", node.getName(), ERR_FILE_NOT_FOUND));
            }
            if (node.isDirectory()) {
                throw new WcException(String.format("'%s': %s", node.getName(), ERR_IS_DIR));
            }
            if (!node.canRead()) {
                throw new WcException(String.format("'%s': %s", node.getName(), ERR_READING_FILE));
            }

            try (InputStream input = IOUtils.openInputStream(file)) {
                long[] count = getCountReport(input);
                output.add(String.format("%s %s", formatCount(isLines, isWords, isBytes, count), file));
                addToTotal(count);
                IOUtils.closeInputStream(input);
            } catch (ShellException | IOException e) {
                throw new WcException(e.getMessage(), e);
            }
        }

        if (fileName.length > 1) {
            output.add(String.format("%s total", formatCount(isLines, isWords, isBytes, totals)));
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input.
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @return A formatted string representing the counts
     * @throws WcException
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin)
            throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }

        long[] count = getCountReport(stdin);
        addToTotal(count);
        return formatCount(isLines, isWords, isBytes, count);
    }

    /**
     * Returns string containing the number of lines, words, and bytes in files and standard input.
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names (including "-" for reading from stdin)
     * @return A formatted string representing the counts
     * @throws WcException
     */
    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                        InputStream stdin, String... fileName) throws WcException {
        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            try {
                if (("-").equals(file)) {
                    output.add(countFromStdin(isBytes, isLines, isWords, stdin) + " -");
                } else {
                    output.add(countFromFiles(isBytes, isLines, isWords, file));
                }
            } catch (WcException e) {
                output.add(e.getMessage());
            }
        }

        if (fileName.length > 1) {
            String totalCount = String.format("%s total", formatCount(isLines, isWords, isBytes, totals));
            output.add(totalCount);
        }

        return String.join(STRING_NEWLINE, output);
    }

    private void addToTotal(long... count) {
        totals[LINES_INDEX] += count[LINES_INDEX];
        totals[WORDS_INDEX] += count[WORDS_INDEX];
        totals[BYTES_INDEX] += count[BYTES_INDEX];
    }
}
