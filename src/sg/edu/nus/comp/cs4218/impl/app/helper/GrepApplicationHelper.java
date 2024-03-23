package sg.edu.nus.comp.cs4218.impl.app.helper;

import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.CASE_INSEN_IDENT;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.CASE_INSEN_IDX;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.COUNT_IDENT;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.COUNT_INDEX;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.GREP_STRING;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.PREFIX_FN;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.PREFIX_FN_IDX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;

public final class GrepApplicationHelper {

    private GrepApplicationHelper() { /* Does nothing */ }

    /**
     * Extract the lines and count number of lines for grep from files and insert them into
     * lineResults and countResults respectively.
     *
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param lineResults       a StringJoiner of the grep line results
     * @param countResults      a StringJoiner of the grep line count results
     * @param fileNames         a String Array of file names supplied by user
     */
    public static void grepResultsFromFiles(String pattern, Boolean isCaseInsensitive, StringJoiner lineResults,
                                            StringJoiner countResults, Boolean isPrefixFileName, String... fileNames)
            throws GrepException {
        boolean isSingleFile = (fileNames.length == 1);
        for (String f : fileNames) {
            String path = convertToAbsolutePath(f);
            File file = new File(path);

            if (!file.exists()) {
                lineResults.add(GREP_STRING + f + ": " + ERR_FILE_NOT_FOUND);
                countResults.add(GREP_STRING + f + ": " + ERR_FILE_NOT_FOUND);
                continue;
            }

            if (!file.canRead()) {
                lineResults.add(GREP_STRING + f + ": " + ERR_NO_PERM);
                countResults.add(GREP_STRING + f + ": " + ERR_NO_PERM);
                continue;
            }

            if (file.isDirectory()) { // ignore if it's a directory
                lineResults.add(GREP_STRING + f + ": " + IS_DIRECTORY);
                countResults.add(GREP_STRING + f + ": " + IS_DIRECTORY);
                continue;
            }

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
                grepResults(pattern, isCaseInsensitive, lineResults, countResults, isPrefixFileName, isSingleFile,
                        f, bufferedReader);
            } catch (PatternSyntaxException pse) {
                throw new GrepException(ERR_INVALID_REGEX, pse);
            } catch (FileNotFoundException e) {
                throw new GrepException(ERR_FILE_NOT_FOUND, e);
            } catch (IOException e) {
                throw new GrepException(ERR_IO_EXCEPTION, e);
            }
        }
    }

    private static void grepResults(String pattern, Boolean isCaseInsensitive, StringJoiner lineResults,
                                    StringJoiner countResults, Boolean isPrefixFileName, Boolean isSingleFile, String fileName,
                                    BufferedReader reader)
            throws IOException {
        String line;
        Pattern compiledPattern;
        if (isCaseInsensitive) {
            compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            compiledPattern = Pattern.compile(pattern);
        }
        int count = 0;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = compiledPattern.matcher(line);
            if (matcher.find()) { // match
                if (isSingleFile && !isPrefixFileName) {
                    lineResults.add(line);
                } else {
                    lineResults.add(fileName + ": " + line);
                }
                count++;
            }
        }
        if (isSingleFile && !isPrefixFileName) {
            countResults.add("" + count);
        } else {
            countResults.add(fileName + ": " + count);
        }
    }

    /**
     * Converts filename to absolute path, if initially was relative path
     *
     * @param fileName supplied by user
     * @return a String of the absolute path of the filename
     */
    private static String convertToAbsolutePath(String fileName) {
        Path path = Paths.get(fileName);

        if (!path.isAbsolute()) {
            String home = System.getProperty("user.home").trim();
            String currentDir = Environment.currentDirectory.trim();
            String convertedPath = convertPathToSystemPath(fileName);

            String newPath;
            if (convertedPath.length() >= home.length() && convertedPath.substring(0, home.length()).trim().equals(home)) {
                newPath = convertedPath;
            } else {
                newPath = currentDir + CHAR_FILE_SEP + convertedPath;
            }
            return newPath;
        }

        return fileName;
    }

    /**
     * Converts path provided by user into path recognised by the system
     *
     * @param path supplied by user
     * @return a String of the converted path
     */
    private static String convertPathToSystemPath(String path) {
        String convertedPath = path;
        String pathIdentifier = "\\" + CHAR_FILE_SEP;
        convertedPath = convertedPath.replaceAll("(\\\\)+", pathIdentifier);
        convertedPath = convertedPath.replaceAll("/+", pathIdentifier);

        if (convertedPath.length() != 0 && convertedPath.charAt(convertedPath.length() - 1) == CHAR_FILE_SEP) {
            convertedPath = convertedPath.substring(0, convertedPath.length() - 1);
        }

        return convertedPath;
    }

    /**
     * Separates the arguments provided by user into the flags, pattern and input files.
     *
     * @param args       supplied by user
     * @param grepFlags  a bool array of possible flags in grep
     * @param inputFiles a ArrayList<String> of file names supplied by user
     * @return regex pattern supplied by user. An empty String if not supplied.
     */
    public static String getGrepArguments(String[] args, boolean[] grepFlags, ArrayList<String> inputFiles)
            throws GrepException {
        String pattern = null;
        boolean isFile = false; // files can only appear after pattern

        for (String s : args) {
            char[] arg = s.toCharArray();
            if (isFile) {
                inputFiles.add(s);
            } else {
                if (!s.isEmpty() && arg[0] == CHAR_FLAG_PREFIX) {
                    arg = Arrays.copyOfRange(arg, 1, arg.length);
                    for (char c : arg) {
                        switch (c) {
                        case CASE_INSEN_IDENT:
                            grepFlags[CASE_INSEN_IDX] = true;
                            break;
                        case COUNT_IDENT:
                            grepFlags[COUNT_INDEX] = true;
                            break;
                        case PREFIX_FN:
                            grepFlags[PREFIX_FN_IDX] = true;
                            break;
                        default:
                            throw new GrepException(ERR_SYNTAX);
                        }
                    }
                } else { // pattern must come before file names
                    pattern = s;
                    isFile = true; // next arg onwards will be files
                }
            }
        }
        return pattern;
    }
}
