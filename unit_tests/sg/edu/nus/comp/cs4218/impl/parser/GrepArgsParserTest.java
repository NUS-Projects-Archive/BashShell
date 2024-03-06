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

    private GrepArgsParser parser;

    @BeforeEach
    void setUp() {
        parser = new GrepArgsParser();
    }

    @Test
    void isInvert_FlagsContainIsInvert_ReturnsTrue() {
        // Given
        parser.flags.add('v');

        // When
        boolean result = parser.isInvert();

        // Then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(chars = {'V', 'X'})
    void isInvert_FlagsDoesContainIsInvert_ReturnsFalse(char flag) {
        // Given
        parser.flags.add(flag);

        // When
        boolean result = parser.isInvert();

        // Then
        assertFalse(result);
    }

    @Test
    void getPattern_NonFlagArgsIsEmpty_ReturnsNull() {
        // When
        String result = parser.getPattern();

        // Then
        assertNull(result);
    }

    @Test
    void getPattern_NonFlagArgsIsNotEmpty_ReturnsFirstNonFlagArg() {
        // Given
        final String pattern = "a*b";
        parser.nonFlagArgs.add("a*b");

        // When
        String result = parser.getPattern();

        // Then
        assertEquals(pattern, result);
    }

    @Test
    void getFileNames_NonFlagArgsSizeLessOrEqualsToOne_ReturnsNull() {
        // When
        String[] result = parser.getFileNames();

        // Then
        assertNull(result);
    }

    @Test
    void getFileNames_NonFlagArgsSizeMoreThanOne_ReturnsSecondNonFlagArgsOnwards() {
        // Given
        final String[] fileNames = {"file1.txt", "file2.txt"};
        parser.nonFlagArgs.add("a*b");
        parser.nonFlagArgs.addAll(Arrays.asList(fileNames));

        // When
        String[] result = parser.getFileNames();

        // Then
        assertArrayEquals(fileNames, result);
    }
}
