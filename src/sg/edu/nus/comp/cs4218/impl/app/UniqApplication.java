package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
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
        try {
            if (inputFile == null) {
                output = uniqFromStdin(isCount, isRepeated, isAllRepeated, stdin, outputFile);
            } else {
                output = uniqFromFile(isCount, isRepeated, isAllRepeated, inputFile, outputFile);
            }
        } catch (Exception e) {
            throw new UniqException(e.getMessage());//NOPMD
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
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param inputFileName  of path to input file
     * @param outputFileName of path to output file (if any)
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String uniqFromFile(final Boolean isCount, final Boolean isRepeated, final Boolean isAllRepeated,
                               final String inputFileName, final String outputFileName)
            throws AbstractApplicationException {
        // TODO: To implement
        return null;
    }

    /**
     * Return filtered unique lines from the standard input
     *
     * @param isCount       Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated    Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param stdin         InputStream containing arguments from Stdin
     * @param outputFileName of path to output file (if any)
     * @return
     * @throws AbstractApplicationException
     */
    @Override
    public String uniqFromStdin(final Boolean isCount, final Boolean isRepeated, final Boolean isAllRepeated,
                                final InputStream stdin, final String outputFileName)
            throws AbstractApplicationException {
        // TODO: To implement
        return null;
    }
}
