package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.AssertUtils.assertEmptyString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.WcException;

class WcApplicationHelperTest {

    private static final String NUMBER_FORMAT = " %7d";
    private static final long[] COUNT = {3, 11, 57};

    private static String formatString(int lineCount, int wordCount, int byteCount) {
        StringBuilder stringBuilder = new StringBuilder();
        if (lineCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, lineCount));
        }
        if (wordCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, wordCount));
        }
        if (byteCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, byteCount));
        }
        return stringBuilder.toString();
    }

    @Test
    void getCountReport_NullInputStream_ThrowsWcException() {
        WcException result = assertThrows(WcException.class, () -> WcApplicationHelper.getCountReport(null));
        String expected = "wc: Null Pointer Exception";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void getCountReport_EmptyInputStream_ReturnsAllZero() {
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        long[] result = assertDoesNotThrow(() -> WcApplicationHelper.getCountReport(stdin));
        long[] expected = {0, 0, 0};
        assertArrayEquals(expected, result);
    }

    @Test
    void getCountReport_ValidInputStream_ReturnsCorrectCount() {
        String content = "This is a sample text\nTo test Wc Application\n For CS4218\n";
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        long[] result = assertDoesNotThrow(() -> WcApplicationHelper.getCountReport(stdin));
        long[] expected = {3, 11, 57};
        assertArrayEquals(expected, result);
    }

    @Test
    void formatCount_NoFlags_ReturnsEmptyString() {
        String actual = WcApplicationHelper.formatCount(false, false, false, COUNT);
        assertEmptyString(actual);
    }

    @Test
    void formatCount_LinesFlagOnly_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(true, false, false, COUNT);
        String expected = formatString(3, -1, -1);
        assertEquals(expected, actual);
    }

    @Test
    void formatCount_WordsFlagOnly_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(false, true, false, COUNT);
        String expected = formatString(-1, 11, -1);
        assertEquals(expected, actual);
    }

    @Test
    void formatCount_BytesFlagOnly_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(false, false, true, COUNT);
        String expected = formatString(-1, -1, 57);
        assertEquals(expected, actual);
    }

    @Test
    void formatCount_LineAndWordFlags_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(true, true, false, COUNT);
        String expected = formatString(3, 11, -1);
        assertEquals(expected, actual);
    }

    @Test
    void formatCount_LineAndByteFlags_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(true, false, true, COUNT);
        String expected = formatString(3, -1, 57);
        assertEquals(expected, actual);
    }

    @Test
    void formatCount_WordAndByteFlags_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(false, true, true, COUNT);
        String expected = formatString(-1, 11, 57);
        assertEquals(expected, actual);
    }

    @Test
    void formatCount_AllFlags_ReturnsFormattedCount() {
        String actual = WcApplicationHelper.formatCount(true, true, true, COUNT);
        String expected = formatString(3, 11, 57);
        assertEquals(expected, actual);
    }
}