package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.fileSeparator;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.isBlank;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.isNumber;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.multiplyChar;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.tokenize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class StringUtilsTest {

    private static final String SINGLE_TRAIL_STR = "stringstr";
    private static final String MULTI_TRAIL_STR = "stringstrstrstr";
    private static final String STRING = "string";
    private static final String SEQUENCE = "str";

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void fileSeparator_WindowsOS_ReturnsExpectedFileSeparator() {
        String expected = "\\\\";
        String actual = fileSeparator();
        assertEquals(expected, actual);
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void fileSeparator_NonWindowsOS_ReturnsExpectedFileSeparator() {
        String expected = "/";
        String actual = fileSeparator();
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"", " ", "      "})
    void isBlank_BlankString_ReturnsTrue(String string) {
        assertTrue(isBlank(string));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "a", "ABC", " xyz ", "> <", "!@#$%^&*()_+=~`?/\\|,."})
    void isBlank_NonBlankString_ReturnsFalse(String string) {
        assertFalse(isBlank(string));
    }

    @Test
    void multiplyChar_SingleLetterA_ReturnsCorrectRepetitionOfLetterA() {
        assertEquals("AAAAA", multiplyChar('A', 5));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"", " ", "      "})
    void tokenize_BlankString_ReturnsEmptyStringArray(String string) {
        assertArrayEquals(new String[0], tokenize(string));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello world", "SOFTWARE ENGINEERING", "CS4218"})
    void tokenize_NonBlankString_ReturnsEmptyStringArray(String string) {
        String[] tokens = tokenize(string);
        assertTrue(tokens.length > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "000", " 456 "})
    void isNumber_StringIsANumber_ReturnsTrue(String string) {
        assertTrue(isNumber(string));
    }

    @ParameterizedTest
    @ValueSource(strings = {"One", "CS4218", "123 456 789", "1,000,000"})
    void isNumber_StringNotANumber_ReturnsFalse(String string) {
        assertFalse(isNumber(string));
    }

    @Test
    void isNumber_EmptyString_ReturnsFalse() {
        assertFalse(isNumber(""));
    }
}