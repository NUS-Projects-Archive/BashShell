package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The sort command orders the lines of the specified files or input
 * and prints the same lines but in sorted order.
 *
 * <p>
 * <b>Command format:</b> <code>sort [Options] [FILES]</code>
 * </p>
 */
public class SortApplication implements SortInterface {

    /**
     * Runs the sort application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws SortException If an I/O exception occurs
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws SortException {
        if (stdin == null) {
            throw new SortException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new SortException(ERR_NO_OSTREAM);
        }

        // Parse argument(s) provided
        final SortArgsParser parser = new SortArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new SortException(e.getMessage(), e);
        }
        final StringBuilder output = new StringBuilder();
        final Boolean isFirstWordNumber = parser.isFirstWordNumber();
        final Boolean isReverseOrder = parser.isReverseOrder();
        final Boolean isCaseIndependent = parser.isCaseIndependent();
        final String[] files = listToArray(parser.getFileNames());

        if (files.length == 0) {
            output.append(sortFromStdin(isFirstWordNumber, isReverseOrder, isCaseIndependent, stdin));
        } else {
            output.append(sortFromFiles(isFirstWordNumber, isReverseOrder, isCaseIndependent, files));
        }

        try {
            if (output.length() != 0) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new SortException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string containing the orders of the lines of the specified file
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param fileNames         Array of String of file names
     * @throws SortException If there are issues sorting lines, e.g., null arguments, file not found,
     *                       encountering a directory, lack of read permissions, or I/O errors
     */
    @Override
    public String sortFromFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                String... fileNames) throws SortException {
        if (fileNames == null || fileNames.length == 0) {
            throw new SortException(ERR_NULL_ARGS);
        }
        List<String> lines = new ArrayList<>();
        for (String file : fileNames) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new SortException(String.format("'%s': %s", node.getName(), ERR_FILE_NOT_FOUND));
            }
            if (node.isDirectory()) {
                throw new SortException(String.format("'%s': %s", node.getName(), ERR_IS_DIR));
            }
            if (!node.canRead()) {
                throw new SortException(String.format("'%s': %s", node.getName(), ERR_READING_FILE));
            }

            try (InputStream input = IOUtils.openInputStream(file)) {
                lines.addAll(IOUtils.getLinesFromInputStream(input));
                IOUtils.closeInputStream(input);
            } catch (ShellException | IOException e) {
                throw new SortException(e.getMessage(), e);
            }
        }
        sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Returns string containing the orders of the lines from the standard input
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @throws SortException If null stream or I/O error occurs
     */
    @Override
    public String sortFromStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                InputStream stdin) throws SortException {
        if (stdin == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }
        List<String> lines = null;
        try {
            lines = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            throw new SortException(ERR_IO_EXCEPTION, e);
        }
        sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Sorts the input ArrayList based on the given conditions. Invoking this function will mutate the ArrayList.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param input             ArrayList of Strings of lines
     */
    private void sortInputString(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                 List<String> input) {
        Collections.sort(input, new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                String temp1 = isCaseIndependent && !isFirstWordNumber ? str1.toLowerCase(Locale.ROOT) : str1;
                String temp2 = isCaseIndependent && !isFirstWordNumber ? str2.toLowerCase(Locale.ROOT) : str2;

                // Extract the first group of numbers if possible.
                if (isFirstWordNumber && !temp1.isEmpty() && !temp2.isEmpty()) {
                    String chunk1 = getChunk(temp1);//NOPMD
                    String chunk2 = getChunk(temp2);//NOPMD
                    boolean isChunk1Numeric = !chunk1.isEmpty() && Character.isDigit(chunk1.charAt(0));
                    boolean isChunk2Numeric = !chunk2.isEmpty() && Character.isDigit(chunk2.charAt(0));

                    int result = 0;
                    if (isChunk1Numeric && !isChunk2Numeric) {
                        return 1;
                    } else if (!isChunk1Numeric && isChunk2Numeric) {
                        return -1;
                    } else if (isChunk1Numeric && isChunk2Numeric) {
                        // If both chunks can be represented as numbers, sort them numerically.
                        result = new BigInteger(chunk1).compareTo(new BigInteger(chunk2));
                    } else {
                        result = chunk1.compareTo(chunk2);
                    }
                    if (result != 0) {
                        return result;
                    }
                    return temp1.substring(chunk1.length()).compareTo(temp2.substring(chunk2.length()));
                }

                return temp1.compareTo(temp2);
            }
        });
        if (isReverseOrder) {
            Collections.reverse(input); // Apply reverse order here after sorting
        }
    }

    /**
     * Extracts a chunk of numbers or non-numbers from str starting from index 0.
     *
     * @param str Input string to read from
     * @return A string representing the extracted chunk
     */
    private String getChunk(String str) {
        int startIndexLocal = 0;
        StringBuilder chunk = new StringBuilder();
        final int strLen = str.length();
        char chr = str.charAt(startIndexLocal++);
        chunk.append(chr);
        final boolean extractDigit = Character.isDigit(chr);
        while (startIndexLocal < strLen) {
            chr = str.charAt(startIndexLocal++);
            if ((extractDigit && !Character.isDigit(chr)) || (!extractDigit && Character.isDigit(chr))) {
                break;
            }
            chunk.append(chr);
        }
        return chunk.toString();
    }
}
