package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.getGrepArguments;
import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.grepResultsFromFiles;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_OUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.GrepException;

public class GrepApplication implements GrepInterface {

    public static final String GREP_STRING = "grep: ";
    public static final String IS_DIRECTORY = "Is a directory";

    public static final char CASE_INSEN_IDENT = 'i';
    public static final char COUNT_IDENT = 'c';
    public static final char PREFIX_FN = 'H';
    private static final int NUM_ARGUMENTS = 3;
    public static final int CASE_INSEN_IDX = 0;
    public static final int COUNT_INDEX = 1;
    public static final int PREFIX_FN_IDX = 2;

    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines,
                                Boolean isPrefixFileName, String... fileNames) throws GrepException {
        if (fileNames == null || fileNames.length == 0 || pattern == null) {
            throw new GrepException(ERR_NULL_STREAMS);
        }
        if (pattern.isEmpty()) {
            throw new GrepException(ERR_EMPTY_PATTERN);
        }

        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);

        grepResultsFromFiles(pattern, isCaseInsensitive, lineResults, countResults, isPrefixFileName, fileNames);

        String results = "";
        if (isCountLines) {
            results = countResults + STRING_NEWLINE;
        } else {
            if (!lineResults.toString().isEmpty()) {
                results = lineResults + STRING_NEWLINE;
            }
        }
        return results;
    }

    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines,
                                Boolean isPrefixFileName, InputStream stdin) throws GrepException {
        if (pattern == null) {
            throw new GrepException(ERR_NULL_STREAMS);
        }
        if (pattern.isEmpty()) {
            throw new GrepException(ERR_EMPTY_PATTERN);
        }

        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            Pattern compiledPattern = isCaseInsensitive
                    ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(pattern);
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) { // match
                    if (isPrefixFileName) {
                        stringJoiner.add(STRING_STDIN_OUT + ": " + line);
                    } else {
                        stringJoiner.add(line);
                    }
                    count++;
                }
            }
        } catch (PatternSyntaxException pse) {
            throw new GrepException(ERR_INVALID_REGEX, pse);
        } catch (NullPointerException npe) {
            throw new GrepException(ERR_FILE_NOT_FOUND, npe);
        } catch (IOException e) {
            throw new GrepException(ERR_IO_EXCEPTION, e);
        }

        String results = "";
        if (isCountLines) {
            if (isPrefixFileName) {
                results = STRING_STDIN_OUT + ": ";
            }
            results += count + STRING_NEWLINE;
        } else {
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner + STRING_NEWLINE;
            }
        }
        return results;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws GrepException {
        boolean[] grepFlags = new boolean[NUM_ARGUMENTS];
        ArrayList<String> inputFiles = new ArrayList<>();
        String pattern = getGrepArguments(args, grepFlags, inputFiles);

        if (stdin == null && inputFiles.isEmpty()) {
            throw new GrepException(ERR_NO_INPUT);
        }
        if (pattern == null) {
            throw new GrepException(ERR_SYNTAX);
        }
        if (pattern.isEmpty()) {
            throw new GrepException(ERR_EMPTY_PATTERN);
        }

        String result;
        if (inputFiles.isEmpty()) {
            result = grepFromStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX],
                    grepFlags[PREFIX_FN_IDX], stdin);
        } else {
            String[] inputFilesArray = new String[inputFiles.size()];
            inputFilesArray = inputFiles.toArray(inputFilesArray);
            result = grepFromFileAndStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX],
                    grepFlags[PREFIX_FN_IDX], stdin, inputFilesArray);
        }

        try {
            stdout.write(result.getBytes());
        } catch (IOException e) {
            throw new GrepException(ERR_WRITE_STREAM, e);
        }
    }

    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines,
                                       Boolean isPrefixFileName, InputStream stdin, String... fileNames)
            throws GrepException {
        if (pattern.isEmpty()) {
            throw new GrepException(ERR_EMPTY_PATTERN);
        }

        StringBuilder result = new StringBuilder();
        Boolean newIsPfxFileName = isPrefixFileName || fileNames.length > 1;
        for (String fileName : fileNames) {
            if ("-".equals(fileName)) {
                result.append(grepFromStdin(pattern, isCaseInsensitive, isCountLines, newIsPfxFileName, stdin));
            } else {
                result.append(grepFromFiles(pattern, isCaseInsensitive, isCountLines, newIsPfxFileName, fileName));
            }
        }
        return result.toString();
    }
}
