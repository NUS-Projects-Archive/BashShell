package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.exception.UniqException.COUNT_ALL_DUP_ERR;
import static sg.edu.nus.comp.cs4218.exception.UniqException.PROB_UNIQ_FILE;
import static sg.edu.nus.comp.cs4218.exception.UniqException.PROB_UNIQ_STDIN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;

/**
 * The uniq command filters adjacent matching lines from INPUT_FILE (or standard input)
 * and writes to an OUTPUT_FILE (or to standard output).
 *
 * <p>
 * <b>Command format:</b> <code>uniq [Options] [INPUT_FILE [OUTPUT_FILE]]<code>
 * </p>
 */
public class UniqApplication implements UniqInterface {

    /**
     * Runs the uniq application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     *               if no files are specified.
     * @throws UniqException When {@code stdin} is null, or
     *                       when cannot find {@code inputFileName}, or
     *                       when IO problems while writing to {@code outputFileName}, or
     *                       when arguments for both count and allRepeated are provided
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {
        // Parse argument(s) provided
        final UniqArgsParser parser = new UniqArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new UniqException(e.getMessage(), e);
        }

        String output;
        final Boolean isCount = parser.isPrefixWithOccurrencesCount();
        final Boolean isRepeated = parser.isPrintDuplicateOncePerGroup();
        final Boolean isAllRepeated = parser.isPrintAllDuplicate();
        final String inputFile = parser.getInputFileName();
        final String outputFile = parser.getOutputFileName();

        // Find uniq
        if (inputFile == null) {
            output = uniqFromStdin(isCount, isRepeated, isAllRepeated, stdin, outputFile);
        } else {
            output = uniqFromFile(isCount, isRepeated, isAllRepeated, inputFile, outputFile);
        }

        // Print results if no output file specified
        try {
            if (outputFile == null) {
                if (stdout == null) {
                    throw new UniqException(ERR_NO_OSTREAM);
                }
                stdout.write(output.getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new UniqException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns filtered unique lines of the specified file
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is true)
     * @param inputFileName  String of path to input file
     * @param outputFileName String of path to output file
     * @return String of the results. Null if {@code outputFileName} is given.
     * @throws UniqException When cannot find {@code inputFileName}, or when IO problems while writing to {@code outputFileName}
     */
    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName,
                               String outputFileName) throws UniqException {
        try {
            String result;

            try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
                result = uniq(isCount, isRepeated, isAllRepeated, reader);
            }

            if (outputFileName == null) {
                // No output file specified, return result.
                return result;
            }

            // Write to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
                writer.write(result);
            }
        } catch (FileNotFoundException e) {
            throw new UniqException(PROB_UNIQ_FILE + ERR_FILE_NOT_FOUND, e);
        } catch (IOException e) {
            throw new UniqException(PROB_UNIQ_FILE + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Returns filtered unique lines from the standard input
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is true)
     * @param stdin          InputStream containing arguments from Stdin
     * @param outputFileName String of path to output file
     * @return String of the results. Null if {@code outputFile} is given.
     * @throws UniqException When {@code stdin} is null, or when IO problems while writing to {@code outputFileName}
     */
    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin,
                                String outputFileName) throws UniqException {
        if (stdin == null) {
            throw new UniqException(ERR_NO_INPUT);
        }

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(stdin));
            String result = uniq(isCount, isRepeated, isAllRepeated, input);

            if (outputFileName == null) {
                // No output file specified, return result.
                return result;
            }

            // Write to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
                writer.write(result);
            }

            return null;
        } catch (IOException e) {
            throw new UniqException(PROB_UNIQ_STDIN + e.getMessage(), e);
        }
    }

    /**
     * Reads input from {@code BufferedReader} and returns the output
     *
     * @param isCount       Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated    Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is true)
     * @param content       BufferedReader holding the content to be processed
     * @return String of the results
     * @throws IOException When both {@code isCount} and {@code isAllRepeated} are true
     */
    private String uniq(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, BufferedReader content)
            throws IOException, UniqException {
        if (isCount && isAllRepeated) {
            throw new UniqException(COUNT_ALL_DUP_ERR);
        }

        int prevCount, count = 0;
        String prevLine, line = null;
        StringBuilder stringBuilder = new StringBuilder();

        do {
            prevLine = line;
            line = content.readLine();

            // First line does not have anything to read
            if (prevLine == null && line == null) { break; }

            // First line
            if (prevLine == null) { prevLine = line; }

            // Duplicate line: track line -> check next line
            if (line != null && line.compareTo(prevLine) == 0) {
                count += 1;
                continue;
            }

            // New unique line: track line -> check flags -> output line
            prevCount = count;
            count = 1;

            // isAllRepeated overrides isRepeated
            if (isAllRepeated) {
                if (prevCount >= 2) {
                    stringBuilder.append((prevLine + STRING_NEWLINE).repeat(prevCount));
                }
                continue;
            } else if (isRepeated && prevCount < 2) {
                continue;
            }

            if (isCount) {
                stringBuilder.append(prevCount).append(' ');
            }
            stringBuilder.append(prevLine).append(STRING_NEWLINE);

        } while (line != null);

        return stringBuilder.toString().trim();
    }
}
