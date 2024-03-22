package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.isBlank;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.isNumber;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.multiplyChar;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.tokenize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class StringUtilsTest {

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
    public void removeTrailing_NullInput_returnsString() {
        String result = StringUtils.removeTrailing("f-3-", null);
        assertEquals("f-3-", result);
        assertNotNull(result);
    }
}
