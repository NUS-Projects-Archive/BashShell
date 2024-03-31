package sg.edu.nus.comp.cs4218.testutils;

import java.io.File;

public final class TestStringUtils {

    public static final String STRING_NEWLINE = System.lineSeparator();
    public static final String STRING_CURR_DIR = ".";
    public static final String STRING_TAB = "\t";
    public static final char CHAR_FILE_SEP = File.separatorChar;
    public static final char CHAR_TAB = '\t';

    private TestStringUtils() { /* Does nothing */ }

    /**
     * Recursively removes trailing instances of given sequence.
     * Returns the original string if it does not end with the sequence.
     *
     * @param str      String to remove sequence from
     * @param sequence String of sequence to remove
     * @return String of result with trailing sequence removed
     */
    public static String removeTrailing(String str, String sequence) {
        if (str == null || sequence == null) {
            return str;
        }

        final int strLength = str.length();
        final int seqLength = sequence.length();
        return str.endsWith(sequence)
                ? removeTrailing(str.substring(0, strLength - seqLength), sequence)
                : str;
    }

    /**
     * Removes trailing instances of given sequence.
     * Returns the original string if it does not end with the sequence.
     *
     * @param str      String to remove sequence from
     * @param sequence String of sequence to remove
     * @return String of result with trailing sequence removed
     */
    public static String removeTrailingOnce(String str, String sequence) {
        if (str == null || sequence == null) {
            return str;
        }

        final int strLength = str.length();
        final int seqLength = sequence.length();
        return str.endsWith(sequence) ? str.substring(0, strLength - seqLength) : str;
    }


    /**
     * Joins an array of strings into a single string with each element separated by a newline character.
     *
     * @param strings the strings to be joined
     * @return a string containing all the input strings joined by newline characters
     */
    public static String joinStringsByNewline(String... strings) {
        return String.join(STRING_NEWLINE, strings);
    }

    /**
     * Joins an array of strings into a single string with each element separated by a tab character.
     *
     * @param strings the strings to be joined
     * @return a string containing all the input strings joined by tab characters
     */
    public static String joinStringsByTab(String... strings) {
        return String.join(STRING_TAB, strings);
    }
}
