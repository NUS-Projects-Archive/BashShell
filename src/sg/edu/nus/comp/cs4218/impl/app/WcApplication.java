package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_GENERAL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
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

public class WcApplication implements WcInterface {

    private static final String NUMBER_FORMAT = " %7d";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;
    private long totalBytes = 0, totalLines = 0, totalWords = 0;

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        // Format: wc [-clw] [FILES]
        if (stdout == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }
        WcArgsParser wcArgsParser = new WcArgsParser();
        try {
            wcArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage(), e);
        }
        String result;
        try {
            if (wcArgsParser.isStdinOnly()) {
                result = countFromStdin(
                        wcArgsParser.isByteCount(),
                        wcArgsParser.isLineCount(),
                        wcArgsParser.isWordCount(),
                        stdin);
            } else {
                result = countFromFileAndStdin(
                        wcArgsParser.isByteCount(),
                        wcArgsParser.isLineCount(),
                        wcArgsParser.isWordCount(),
                        stdin,
                        wcArgsParser.getFileNames().toArray(new String[0]));
            }
        } catch (Exception e) {
            // Will never happen
            throw new WcException(ERR_GENERAL, e);
        }
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new WcException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names
     * @throws WcException
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, //NOPMD
                                 String... fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                result.add("wc: " + ERR_FILE_NOT_FOUND);
                continue;
            }
            if (node.isDirectory()) {
                result.add("wc: " + ERR_IS_DIR);
                continue;
            }
            if (!node.canRead()) {
                result.add("wc: " + ERR_NO_PERM);
                continue;
            }

            InputStream input = null;
            try {
                input = IOUtils.openInputStream(file);
            } catch (ShellException e) {
                throw new WcException(e.getMessage(), e);
            }
            long[] count = getCountReport(input); // lines words bytes
            try {
                IOUtils.closeInputStream(input);
            } catch (ShellException e) {
                throw new WcException(e.getMessage(), e);
            }

            // Format all output: " %7d %7d %7d %s"
            // Output in the following order: lines words bytes filename
            StringBuilder sb = new StringBuilder(); //NOPMD
            if (isLines) {
                sb.append(String.format(NUMBER_FORMAT, count[0]));
            }
            if (isWords) {
                sb.append(String.format(NUMBER_FORMAT, count[1]));
            }
            if (isBytes) {
                sb.append(String.format(NUMBER_FORMAT, count[2]));
            }
            sb.append(String.format(" %s", file));
            result.add(sb.toString());
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            StringBuilder sb = new StringBuilder(); //NOPMD
            if (isLines) {
                sb.append(String.format(NUMBER_FORMAT, totalLines));
            }
            if (isWords) {
                sb.append(String.format(NUMBER_FORMAT, totalWords));
            }
            if (isBytes) {
                sb.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            sb.append(" total");
            result.add(sb.toString());
        }
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws WcException
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin)
            throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] count = getCountReport(stdin); // lines words bytes;

        StringBuilder sb = new StringBuilder(); //NOPMD
        if (isLines) {
            sb.append(String.format(NUMBER_FORMAT, count[0]));
        }
        if (isWords) {
            sb.append(String.format(NUMBER_FORMAT, count[1]));
        }
        if (isBytes) {
            sb.append(String.format(NUMBER_FORMAT, count[2]));
        }

        return sb.toString();
    }

    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                        InputStream stdin, String... fileName) throws WcException {
        try {
            List<String> result = new ArrayList<>();

            for (String file : fileName) {
                if (file.equals("-")) {
                    result.add(countFromStdin(isBytes, isLines, isWords, stdin) + " -");
                } else {
                    result.add(countFromFiles(isBytes, isLines, isWords, file));
                }
            }
            if (fileName.length > 1) {
                StringBuilder sb = new StringBuilder(); //NOPMD
                if (isLines) {
                    sb.append(String.format(NUMBER_FORMAT, totalLines));
                }
                if (isWords) {
                    sb.append(String.format(NUMBER_FORMAT, totalWords));
                }
                if (isBytes) {
                    sb.append(String.format(NUMBER_FORMAT, totalBytes));
                }
                sb.append(" total");
                result.add(sb.toString());
            }

            return String.join(STRING_NEWLINE, result);
        } catch (WcException e) {
            throw new WcException(e.getMessage(), e);
        }
    }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws WcException
     */
    public long[] getCountReport(InputStream input) throws WcException {
        if (input == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] result = new long[3]; // lines, words, bytes

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int inRead = 0;
        boolean inWord = false;
        try {
            while ((inRead = input.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        // Use <newline> character here. (Ref: UNIX)
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }

                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
                buffer.write(data, 0, inRead);
            }
            buffer.flush();
            if (inWord) {
                ++result[WORDS_INDEX]; // To handle last word
            }
        } catch (IOException e) {
            throw new WcException(ERR_IO_EXCEPTION, e);
        }
        totalWords += result[WORDS_INDEX];
        totalBytes += result[BYTES_INDEX];
        totalLines += result[LINES_INDEX];
        return result;
    }
}
