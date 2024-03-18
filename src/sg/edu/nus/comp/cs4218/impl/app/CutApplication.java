package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.CutApplicationHelper.cutSelectedPortions;
import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
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

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * The cut command cuts out selected portions of each line (as specified by list)
 * from each file and writes them to the standard output.
 *
 * <p>
 * <b>Command format:</b> <br>
 * <code>cut Option LIST FILES...</code> <br>
 * </p>
 */
public class CutApplication implements CutInterface {

    /**
     * Runs the cut application with the specified arguments.
     *
     * @param args   Array of arguments for the application
     * @param stdin  An InputStream. The input for the command is read from this InputStream
     *               if no files are specified
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws CutException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {
        if (args == null || args.length == 0) {
            throw new CutException(ERR_NULL_ARGS);
        }
        if (args.length < 2) {
            throw new CutException(ERR_NO_ARGS);
        }
        if (stdin == null) {
            throw new CutException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new CutException(ERR_NO_OSTREAM);
        }

        // Parse argument(s) provided
        final CutArgsParser parser = new CutArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CutException(e.getMessage(), e);
        }

        final Boolean isCharPo = parser.isCharPo();
        final Boolean isBytePo = parser.isBytePo();
        final List<int[]> ranges = parser.getRangeList();
        final String[] files = listToArray(parser.getFileNames());

        StringBuilder output = new StringBuilder();
        if (files.length == 0) {
            output.append(cutFromStdin(isCharPo, isBytePo, ranges, stdin));
        } else {
            output.append(cutFromFileAndStdin(isCharPo, isBytePo, ranges, stdin, files));
        }

        try {
            if (output.length() != 0) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new CutException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Cuts out selected portions of each line.
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param fileName Array of String of file names
     * @return A string containing the concatenated output of cut portions from each line
     *         including errors (if any) at the end
     * @throws CutException
     */
    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws CutException {
        validateCutFlags(isCharPo, isBytePo);
        if (fileName == null || fileName.length == 0) {
            throw new CutException(ERR_NULL_ARGS);
        }

        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new CutException(String.format("'%s': %s", node.getName(), ERR_FILE_NOT_FOUND));
            }
            if (node.isDirectory()) {
                throw new CutException(String.format("'%s': %s", node.getName(), ERR_IS_DIR));
            }
            if (!node.canRead()) {
                throw new CutException(String.format("'%s': %s", node.getName(), ERR_READING_FILE));
            }

            try (InputStream input = IOUtils.openInputStream(file)) {
                output.addAll(cutSelectedPortions(isCharPo, isBytePo, ranges, input));
                IOUtils.closeInputStream(input);
            } catch (ShellException | IOException e) {
                throw new CutException(e.getMessage(), e);
            }
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Cuts out selected portions of each line.
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stdin    InputStream containing arguments from Stdin
     * @return A string containing the concatenated output of cut portions from each line,
     *         including errors (if any) at the end
     * @throws CutException
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin)
            throws CutException {
        validateCutFlags(isCharPo, isBytePo);
        if (stdin == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }

        List<String> output = null;
        try {
            output = cutSelectedPortions(isCharPo, isBytePo, ranges, stdin);
        } catch (IOException e) {
            throw new CutException(ERR_IO_EXCEPTION, e);
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Cuts out selected portions of each line.
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return A string containing the concatenated output of cut portions from each line,
     *         including errors (if any) at the end
     * @throws CutException
     */
    public String cutFromFileAndStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges,
                                      InputStream stdin, String... fileName) throws CutException {
        validateCutFlags(isCharPo, isBytePo);
        List<String> output = new ArrayList<>();
        for (String file : fileName) {
            try {
                if ("-".equals(file)) {
                    output.add(cutFromStdin(isCharPo, isBytePo, ranges, stdin));
                } else {
                    output.add(cutFromFiles(isCharPo, isBytePo, ranges, file));
                }
            } catch (CutException e) {
                output.add(e.getMessage());
            }
        }

        return String.join(STRING_NEWLINE, output);
    }

    /**
     * Validates the cut flags to ensure that exactly one flag (cut by character or byte) is selected, but not both.
     *
     * @param isCharPo Boolean flag indicating whether the cut is by character
     * @param isBytePo Boolean flag indicating whether the cut is by byte
     * @throws CutException if both flags are true or both are false
     */
    private void validateCutFlags(Boolean isCharPo, Boolean isBytePo) throws CutException {
        if (isCharPo.equals(isBytePo)) {
            throw new CutException("Exactly one flag (cut by character or byte) should be selected, but not both");
        }
    }
}
