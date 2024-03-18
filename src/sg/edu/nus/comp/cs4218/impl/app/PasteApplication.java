package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.checkPasteFileValidity;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.mergeInParallel;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.mergeInSerial;
import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The paste command merge lines of files, write to standard output lines consisting of
 * sequentially corresponding lines of each given file, separated by a TAB character.
 *
 * <p>
 * <b>Command format:</b> <code>paste [Option] [FILES]...</code>
 * </p>
 */
public class PasteApplication implements PasteInterface {

    /**
     * Runs the paste application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used. If one file is specified, stdin used
     *               for other file.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {
        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }

        final PasteArgsParser parser = new PasteArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage(), e);
        }

        final Boolean isSerial = parser.isSerial();
        final String[] nonFlagArgs = listToArray(parser.getNonFlagArgs());

        final StringBuilder output = new StringBuilder();
        if (nonFlagArgs.length == 0) {
            output.append(mergeStdin(isSerial, stdin));
        } else {
            output.append(mergeFileAndStdin(isSerial, stdin, nonFlagArgs));
        }

        try {
            if (output.length() != 0) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new PasteException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin arguments.
     * If only one Stdin arg is specified, echo back the Stdin.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @throws PasteException If fails to get stdin data
     */
    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws PasteException {
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }

        List<List<String>> output = new ArrayList<>();
        try {
            output.add(IOUtils.getLinesFromInputStream(stdin));
        } catch (IOException e) {
            throw new PasteException(ERR_IO_EXCEPTION, e);
        }

        return isSerial ? mergeInSerial(output) : mergeInParallel(output);
    }


    /**
     * Returns string of line-wise concatenated (tab-separated) files.
     * If only one file is specified, echo back the file content.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param fileName Array of file names to be read and merged (not including "-" for reading from stdin)
     * @throws PasteException
     */
    @Override
    public String mergeFile(Boolean isSerial, String... fileName) throws PasteException {
        if (fileName == null || fileName.length == 0) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        List<List<String>> output = new ArrayList<>();
        for (String file : fileName) {
            if (!checkPasteFileValidity(file)) {
                continue;
            }
            try (InputStream input = IOUtils.openInputStream(file)) {
                output.add(IOUtils.getLinesFromInputStream(input));
                IOUtils.closeInputStream(input);
            } catch (ShellException | IOException e) {
                throw new PasteException(e.getMessage(), e);
            }
        }

        return isSerial ? mergeInSerial(output) : mergeInParallel(output);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files and Stdin arguments.
     * <p>
     * Assumptions:
     * - When processing in parallel and there are multiple standard input ("-") entries,
     *   each standard input entry takes one line at a time sequentially, rotating among them.
     *   For example, the first "-" entry takes the first line, the second "-" entry takes the second line,
     *   and so on. Once all "-" entries have taken one line each, the process repeats.
     * - When processing in serial and there are multiple standard input ("-") entries,
     *   the first standard input entry will consume the entire line
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged (including "-" for reading from stdin)
     * @throws PasteException
     */
    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws PasteException {
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        if (fileName == null || fileName.length == 0) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        // Check to ensure the files (not including "-") are valid
        for (String file : fileName) {
            checkPasteFileValidity(file);
        }

        int numOfStdin = (int) Arrays.stream(fileName).filter("-"::equals).count();
        List<String> stdinData = new ArrayList<>();
        try {
            if (numOfStdin > 0) {
                stdinData = IOUtils.getLinesFromInputStream(stdin);
            }
        } catch (IOException e) {
            throw new PasteException(ERR_IO_EXCEPTION, e);
        }

        int currStdin = 0;
        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            if (("-").equals(file)) {
                List<String> currList = new ArrayList<>();
                int step = isSerial ? 1 : numOfStdin;
                for (int i = currStdin; i < stdinData.size(); i += step) {
                    currList.add(stdinData.get(i));
                }
                currStdin += isSerial ? stdinData.size() : 1;
                output.add(String.join(STRING_NEWLINE, currList));
            } else {
                output.add(mergeFile(isSerial, file));
            }
        }

        List<List<String>> totalLines = output.stream().filter(s -> !s.isEmpty())
                .map(s -> Arrays.asList((s.split(STRING_NEWLINE))))
                .collect(Collectors.toList());

        String result = isSerial ? mergeInSerial(totalLines) : mergeInParallel(totalLines);
        return String.join(STRING_NEWLINE, result);
    }
}
