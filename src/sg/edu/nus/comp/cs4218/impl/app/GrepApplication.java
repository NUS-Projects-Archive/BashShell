package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.getGrepArguments;
import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.grepResultsFromFiles;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_OUTPUT;

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
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;

public class GrepApplication implements GrepInterface {

    public static final String EMPTY_PATTERN = "Pattern should not be empty.";
    public static final String NULL_POINTER = "Null Pointer Exception";

    private static final int NUM_ARGUMENTS = 3;
    private static final int CASE_INSEN_IDX = 0;
    private static final int COUNT_INDEX = 1;
    private static final int PREFIX_FN_IDX = 2;

    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws AbstractApplicationException {
        if (fileNames == null || pattern == null) {
            throw new GrepException(NULL_POINTER);
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
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws AbstractApplicationException {
        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line;
            Pattern compiledPattern;
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }
            while ((line = reader.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) { // match
                    if (isPrefixFileName) {
                        stringJoiner.add(STRING_STDIN_OUTPUT + ": " + line);
                    } else {
                        stringJoiner.add(line);
                    }
                    count++;
                }
            }
            reader.close();
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
                results = STRING_STDIN_OUTPUT + ": ";
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
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        try {
            boolean[] grepFlags = new boolean[NUM_ARGUMENTS];
            ArrayList<String> inputFiles = new ArrayList<>();
            String pattern = getGrepArguments(args, grepFlags, inputFiles);
            String result;

            if (stdin == null && inputFiles.isEmpty()) {
                throw new Exception(ERR_NO_INPUT);
            }
            if (pattern == null) {
                throw new Exception(ERR_SYNTAX);
            }

            if (pattern.isEmpty()) {
                throw new Exception(EMPTY_PATTERN);
            } else {
                if (inputFiles.isEmpty()) {
                    result = grepFromStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin);
                } else {
                    String[] inputFilesArray = new String[inputFiles.size()];
                    inputFilesArray = inputFiles.toArray(inputFilesArray);
                    result = grepFromFileAndStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin, inputFilesArray);
                }
            }
            stdout.write(result.getBytes());
        } catch (GrepException grepException) {
            throw grepException;
        } catch (Exception e) {
            throw new GrepException(e.getMessage(), e);
        }
    }

    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws AbstractApplicationException {
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
