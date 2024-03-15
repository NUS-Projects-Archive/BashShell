package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_GENERAL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_TAB;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils;
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

    private int maxFileLength = 0;
    private final List<List<String>> tempListResult;

    public PasteApplication() {
        this.tempListResult = new ArrayList<>();
    }

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
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException { //NOPMD
        if (stdout == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }
        final PasteArgsParser pasteArgsParser = new PasteArgsParser();

        try {
            pasteArgsParser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage(), e);
        }

        final String[] nonFlagArgs = CollectionsUtils.listToArray(pasteArgsParser.getNonFlagArgs());
        boolean isSerial = pasteArgsParser.isSerial();
        boolean hasStdin = pasteArgsParser.hasStdin();
        String result = null;

        if (nonFlagArgs.length == 0) {
            result = mergeStdin(isSerial, stdin);
        } else if (hasStdin) {
            result = mergeFileAndStdin(isSerial, stdin, nonFlagArgs);
        } else {
            result = mergeFile(isSerial, nonFlagArgs);
        }

        try {
            if (result.length() != 0) {
                stdout.write(result.getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new PasteException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin arguments. If only one Stdin
     * arg is specified, echo back the Stdin.
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

        List<String> data;
        try {
            data = IOUtils.getLinesFromInputStream(stdin);
            tempListResult.add(data);
        } catch (Exception e) {
            throw new PasteException(e.getMessage(), e);
        }
        maxFileLength = Math.max(maxFileLength, data.size());

        return isSerial ? mergeInSerial(tempListResult) : mergeInParallel(tempListResult);
    }


    /**
     * Returns string of line-wise concatenated (tab-separated) files. If only one file is
     * specified, echo back the file content.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param fileName Array of file names to be read and merged (not including "-" for reading from stdin)
     * @throws PasteException
     */
    @Override
    public String mergeFile(Boolean isSerial, String... fileName) throws PasteException {
        if (fileName == null) {
            throw new PasteException(ERR_GENERAL);
        }

        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new PasteException(file + ": " + ERR_FILE_NOT_FOUND);
            }
            if (node.isDirectory()) {
                continue;
            }
            if (!node.canRead()) {
                throw new PasteException(ERR_NO_PERM);
            }

            List<String> fileData;
            InputStream input = null;
            try {
                input = IOUtils.openInputStream(file);
                fileData = IOUtils.getLinesFromInputStream(input);
                IOUtils.closeInputStream(input);
                input.close();
            } catch (ShellException | IOException e) {
                throw new PasteException(e.getMessage(), e);
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    throw new PasteException(e.getMessage(), e);
                }
            }

            maxFileLength = Math.max(maxFileLength, fileData.size());
            tempListResult.add(fileData);
        }

        return isSerial ? mergeInSerial(tempListResult) : mergeInParallel(tempListResult);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files and Stdin arguments.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged (including "-" for reading from stdin)
     * @throws PasteException
     */
    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws PasteException { //NOPMD
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }

        for (String file : fileName) {
            if (file.compareTo("-") == 0) {
                continue;
            }
            if (file.equals("")) {
                throw new PasteException(ERR_GENERAL);
            }

            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new PasteException(file + ": " + ERR_FILE_NOT_FOUND);
            }
            if (node.isDirectory()) {
                continue;
            }
            if (!node.canRead()) {
                throw new PasteException(ERR_NO_PERM);
            }
        }
        List<String> data;
        try {
            data = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            throw new PasteException(e.getMessage(), e);
        }
        int numOfDash = (int) Arrays.stream(fileName).filter("-"::equals).count();
        if (numOfDash != 0) {
            maxFileLength = Math.max(maxFileLength, data.size() / numOfDash);
            if (data.size() % numOfDash != 0) {
                maxFileLength = Math.max(maxFileLength, (data.size() / numOfDash) + 1);
            }
        }
        if (isSerial) {
            numOfDash = 1;
        }
        int currStdin = 0;
        for (String file : fileName) {
            if (file != null && file.equals("-")) {
                List<String> currLst = new ArrayList<>();
                for (int i = currStdin; i < data.size(); i += numOfDash) {
                    currLst.add(data.get(i));
                }
                currStdin = isSerial ? data.size() : currStdin + 1;
                tempListResult.add(currLst);
            } else {
                mergeFile(isSerial, file);
            }
        }
        return isSerial ? mergeInSerial(tempListResult) : mergeInParallel(tempListResult);
    }

    /**
     * Takes in a List of Lists of Strings and merges lists in serial.
     * Each inner list represents a row of data, and each element in the inner list represents a column.
     * Columns within a row are separated by a tab character ('\t'), and rows are separated by a newline character ('\n')
     *
     * @param listResult List of Lists of Strings representing the data to be merged
     * @return Merged data as a single String
     */
    public String mergeInSerial(List<List<String>> listResult) {
        List<List<String>> res = new ArrayList<>();
        for (List<String> lst : listResult) {
            List<String> currList = new ArrayList<>(lst);
            res.add(currList);
        }

        List<String> interRes = new ArrayList<>();
        for (List<String> lst : res) {
            interRes.add(String.join(STRING_TAB, lst));
        }

        String mergedString = String.join(STRING_NEWLINE, interRes);

        if (mergedString.endsWith(STRING_NEWLINE)) {
            mergedString = mergedString.substring(0, mergedString.length() - STRING_NEWLINE.length());
        }

        return mergedString;
    }

    /**
     * Merges lists in parallel, where each sublist corresponds to a column in the merged result.
     * If a sublist does not have an element at a particular index, an empty string is inserted.
     *
     * @param listResult A List of Lists of Strings representing the data to be merged in parallel
     * @return A String representing the merged data with elements separated by tabs and rows separated by newlines
     */
    public String mergeInParallel(List<List<String>> listResult) {
        List<List<String>> res = new ArrayList<>();

        for (int i = 0; i < maxFileLength; i++) {
            List<String> currLstToAdd = new ArrayList<>();
            for (List<String> currLst : listResult) {
                currLstToAdd.add(i < currLst.size() ? currLst.get(i) : "");
            }
            res.add(currLstToAdd);
        }

        List<String> interRes = new ArrayList<>();
        for (List<String> lst : res) {
            interRes.add(String.join(STRING_TAB, lst));
        }

        return String.join(STRING_NEWLINE, interRes);
    }
}
