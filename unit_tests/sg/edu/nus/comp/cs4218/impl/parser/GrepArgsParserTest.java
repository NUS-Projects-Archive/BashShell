package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GrepArgsParserTest {
    private GrepArgsParser grepArgsParser;

    @BeforeEach
    void setUp() {
        grepArgsParser = new GrepArgsParser();
    }

    @Test
    void isInvert_FlagsContainIsInvert_ReturnsTrue() {
        // Given
        grepArgsParser.flags.add('v');

        // When
        boolean result = grepArgsParser.isInvert();

        // Then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(chars = {'V', 'X'})
    void isInvert_FlagsDoesContainIsInvert_ReturnsFalse(char flag) {
        // Given
        grepArgsParser.flags.add(flag);

        // When
        boolean result = grepArgsParser.isInvert();

        // Then
        assertFalse(result);
    }

    @Test
    void getPattern_NonFlagArgsIsEmpty_ReturnsNull() {
        // When
        String result = grepArgsParser.getPattern();

        // Then
        assertEquals(result, null);
    }

    @Test
    void getPattern_NonFlagArgsIsNotEmpty_ReturnsFirstNonFlagArg() {
        // Given
        final String pattern = "a*b";
        grepArgsParser.nonFlagArgs.add("a*b");

        // When
        String result = grepArgsParser.getPattern();

        // Then
        assertEquals(result, pattern);
    }

    @Test
    void getFileNames_NonFlagArgsSizeLessOrEqualsToOne_ReturnsNull() {
        // When
        String[] result = grepArgsParser.getFileNames();

        // Then
        assertNull(result);
    }

    @Test
    void getFileNames_NonFlagArgsSizeMoreThanOne_ReturnsSecondNonFlagArgsOnwards() {
        // Given
        final String[] fileNames = {"file1.txt", "file2.txt"};
        grepArgsParser.nonFlagArgs.add("a*b");
        grepArgsParser.nonFlagArgs.addAll(Arrays.asList(fileNames));

        // When
        String[] result = grepArgsParser.getFileNames();

        // Then
        assertArrayEquals(result, fileNames);
    }
}
