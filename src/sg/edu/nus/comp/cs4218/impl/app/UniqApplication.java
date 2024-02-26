package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.exception.UniqException.MEANINGLESS_COUNT_ALL_DUP;
import static sg.edu.nus.comp.cs4218.exception.UniqException.PROB_UNIQ_FILE;
import static sg.edu.nus.comp.cs4218.exception.UniqException.PROB_UNIQ_STDIN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
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
     * @throws UniqException
     */
    @Override
    public void run(final String[] args, final InputStream stdin, final OutputStream stdout)
            throws UniqException {
        // Format: uniq [Options] [INPUT_FILE [OUTPUT_FILE]]

        // Parse argument(s) provided
        final UniqArgsParser parser = new UniqArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new UniqException(e.getMessage());
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
                stdout.write(output.getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new UniqException(ERR_WRITE_STREAM);//NOPMD
        }
    }

    /**
     * Return filtered unique lines of the specified file
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is true)
     * @param inputFileName  String of path to input file
     * @param outputFileName String of path to output file
     * @return String of the results. Null if {@code outputFile} is given.
     * @throws UniqException
     */
    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName,
                               String outputFileName) throws UniqException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFileName));
            String result = uniq(isCount, isRepeated, isAllRepeated, in);
            in.close();

            if (outputFileName == null) {
                // No output file specified, return result.
                return result;
            }

            // Write to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write(result);
            writer.close();

            return null;
        } catch (FileNotFoundException e) {
            throw new UniqException(PROB_UNIQ_FILE + ERR_READING_FILE);
        } catch (IOException e) {
            throw new UniqException(PROB_UNIQ_FILE + e.getMessage());
        }
    }

    /**
     * Return filtered unique lines from the standard input
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is true)
     * @param stdin          InputStream containing arguments from Stdin
     * @param outputFileName String of path to output file
     * @return String of the results. Null if {@code outputFile} is given.
     * @throws UniqException
     */
    @Override
    public String uniqFromStdin(final Boolean isCount, final Boolean isRepeated, final Boolean isAllRepeated,
                                final InputStream stdin, final String outputFileName) throws UniqException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stdin));
            String result = uniq(isCount, isRepeated, isAllRepeated, in);

            if (outputFileName == null) {
                // No output file specified, return result.
                return result;
            }

            // Write to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write(result);
            writer.close();

            return null;
        } catch (IOException e) {
            throw new UniqException(PROB_UNIQ_STDIN + e.getMessage());
        }
    }

    /**
     * Reads input from {@code BufferedReader} and returns the output
     *
     * @param isCount       Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated    Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is true)
     * @param content       BufferedReader holding the content to be processed
     * @return String of the results.
     * @throws IOException
     */
    private String uniq(final Boolean isCount, final Boolean isRepeated, final Boolean isAllRepeated,
                        final BufferedReader content) throws IOException, UniqException {

        if (isCount && isAllRepeated) {
            throw new UniqException(MEANINGLESS_COUNT_ALL_DUP);
        }

        int prevCount, count = 0;
        String prevLine, line = null;
        StringBuilder stringBuilder = new StringBuilder();

        do {
            prevLine = line;
            line = content.readLine();

            // First line
            if (prevLine == null) {
                prevLine = line;
                count += 1;
                continue;
            }

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
                    for (int i = 0; i < prevCount; i++) {
                        stringBuilder.append(prevLine).append(STRING_NEWLINE);
                    }
                }
                continue;
            } else if (isRepeated && prevCount < 2) {
                continue;
            }

            if (isCount) {
                stringBuilder.append(prevCount).append(" ");
            }

            stringBuilder.append(prevLine).append(STRING_NEWLINE);

        } while (prevLine != null && line != null);

        return stringBuilder.toString().trim();
    }
}
