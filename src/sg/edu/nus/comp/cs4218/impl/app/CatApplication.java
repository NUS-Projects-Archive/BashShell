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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The cat command concatenates its input arguments and writes the output on the standard output, or to an output file.
 *
 * <p>
 * <b>Command format:</b> <code>cat [ARGS]...</code>
 * </p>
 */
public class CatApplication implements CatInterface {

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws CatException If any specified file does not exist, is a directory, is unreadable, or if there
     *                      is an error occurs while reading from the standard input
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new CatException(ERR_NO_OSTREAM);
        }

        final CatArgsParser parser = new CatArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage(), e);
        }

        final Boolean isLineNumber = parser.isLineNumber();
        final String[] files = listToArray(parser.getFiles());

        StringBuilder output = new StringBuilder();
        if (files.length == 0) {
            output.append(catStdin(isLineNumber, stdin));
        } else {
            output.append(catFileAndStdin(isLineNumber, stdin, files));
        }

        try {
            if (output.length() != 0) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new CatException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string containing the content of the specified file.
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param fileName     Array of String of file names (not including "-" for reading from stdin)
     * @return A string containing the concatenated content of the specified file(s),
     *         optionally prefixed with line numbers
     * @throws CatException If any specified file do not exist, is a directory or is unreadable
     */
    @Override
    public String catFiles(Boolean isLineNumber, String... fileName) throws CatException {
        if (fileName == null || fileName.length == 0) {
            throw new CatException(ERR_NULL_ARGS);
        }

        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new CatException(String.format("'%s': %s", node.getName(), ERR_FILE_NOT_FOUND));
            }
            if (node.isDirectory()) {
                throw new CatException(String.format("'%s': %s", node.getName(), ERR_IS_DIR));
            }
            if (!node.canRead()) {
                throw new CatException(String.format("'%s': %s", node.getName(), ERR_READING_FILE));
            }

            try (InputStream input = IOUtils.openInputStream(file)) {
                output.addAll(prefixLineNumber(isLineNumber, input));
                IOUtils.closeInputStream(input);
            } catch (ShellException | IOException e) {
                throw new CatException(e.getMessage(), e);
            }
        }

        return String.join(STRING_NEWLINE, output);
    }


    /**
     * Returns string containing the content of the standard input.
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param stdin        InputStream containing arguments from Stdin
     * @return A string containing the content read from the standard input, optionally prefixed with line numbers
     * @throws CatException If an error occurs while reading from the standard input
     */
    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }

        List<String> output;
        try {
            output = prefixLineNumber(isLineNumber, stdin);
        } catch (IOException e) {
            throw new CatException(ERR_IO_EXCEPTION, e);
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Returns string containing the content of the standard input and specified file.
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param stdin        InputStream containing arguments from Stdin
     * @param fileName     Array of String of file names (including "-" for reading from stdin)
     * @return A string containing the concatenated content of the specified file(s) and stdin(s),
     *         optionally prefixed with line numbers
     * @throws CatException
     */
    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileName) {
        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            try {
                if (("-").equals(file)) {
                    output.add(catStdin(isLineNumber, stdin));
                } else {
                    output.add(catFiles(isLineNumber, file));
                }
            } catch (CatException e) {
                output.add(e.getMessage());
            }
        }

        output.removeIf(String::isEmpty); // remove empty lines that represents cases of blank files
        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Reads lines from the specified {@code inputStream} and optionally
     * prefixes each line with its corresponding line number.
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param inputStream  InputStream to read lines from
     * @return A {@link List} of strings where each line is optionally prefixed with
     *         its line number (if {@code isLineNumber} is {@code true})
     * @throws IOException
     */
    private List<String> prefixLineNumber(Boolean isLineNumber, InputStream inputStream) throws IOException {
        List<String> lines = IOUtils.getLinesFromInputStream(inputStream);

        // Use IntStream.range to create an increasing sequence of numbers
        return IntStream.range(0, lines.size())
                .mapToObj(i -> (isLineNumber ? i + 1 + " " : "") + lines.get(i))
                .collect(Collectors.toList());
    }
}
