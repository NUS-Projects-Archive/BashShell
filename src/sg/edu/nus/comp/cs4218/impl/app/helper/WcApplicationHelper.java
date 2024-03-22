package sg.edu.nus.comp.cs4218.impl.app.helper;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import sg.edu.nus.comp.cs4218.exception.WcException;

public final class WcApplicationHelper {

    private static final String NUMBER_FORMAT = " %7d";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;
    private static final int EOF = -1;

    private WcApplicationHelper() { /* Does nothing*/ }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws WcException
     */
    public static long[] getCountReport(InputStream input) throws WcException {
        if (input == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] result = new long[3]; // lines, words, bytes
        byte[] data = new byte[1024];
        int inRead;
        boolean inWord = false;
        try {
            while ((inRead = input.read(data, 0, data.length)) != EOF) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        // Use <newline> character here. (Ref: UNIX)
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }

                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
            }
            if (inWord) {
                ++result[WORDS_INDEX]; // To handle last word
            }
        } catch (IOException e) {
            throw new WcException(ERR_IO_EXCEPTION, e);
        }

        return result;
    }

    /**
     * Formats the counts based on the specified options.
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param count   An array containing the counts of lines, words, and bytes, in that order
     * @return A formatted string representing the counts based on the specified options
     */
    public static String formatCount(Boolean isLines, Boolean isWords, Boolean isBytes, long... count) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isLines) {
            stringBuilder.append(String.format(NUMBER_FORMAT, count[0]));
        }
        if (isWords) {
            stringBuilder.append(String.format(NUMBER_FORMAT, count[1]));
        }
        if (isBytes) {
            stringBuilder.append(String.format(NUMBER_FORMAT, count[2]));
        }

        return stringBuilder.toString();
    }
}
