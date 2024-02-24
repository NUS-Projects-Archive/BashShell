package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.isBlank;

public class StringUtilsTest {

    @Test
    void isBlank_null_returnsTrue() {
        assertTrue(isBlank(null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "      " })
    void isBlank_blankString_returnsTrue(String s) {
        assertTrue(isBlank(s));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", "a", "ABC", " xyz ", "> <", "!@#$%^&*()_+=~`?/\\|,." })
    void isBlank_nonBlankString_returnsFalse(String s) {
        assertFalse(isBlank(s));
    }

}
