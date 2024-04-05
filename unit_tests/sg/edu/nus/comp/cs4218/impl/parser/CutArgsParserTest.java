package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class CutArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('c', 'b');
    private static final String FLAG_CUT_BY_CHAR = "-c";
    private static final String FLAG_CUT_BY_BYTE = "-b";
    private static final String SINGLE_NUM = "1";
    private static final String MULTI_NUM = "1,5";
    private static final String RANGE_OF_NUM = "1-5";
    private static final String MULTI_AND_RANGE = "1,5,10-15";
    private static final String TOO_LARGE_RANGE = "9223372036854775808"; // 1 value larger than max of Long
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String STDIN = "-";
    private CutArgsParser parser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, MULTI_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, RANGE_OF_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, MULTI_AND_RANGE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, STDIN, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, MULTI_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, RANGE_OF_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, MULTI_AND_RANGE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, STDIN, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE, FILE_TWO})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{SINGLE_NUM, FILE_ONE}), // lacking flag
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, FILE_ONE}), // lacking range
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, FILE_ONE}), // lacking range
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE}), // only 1 flag allowed
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE}), // only 1 flag allowed
                Arguments.of((Object) new String[]{"-C", SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{"-B", SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "0", FILE_ONE}), // position must be numbered from 1
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "0-5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "0,5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-1", FILE_ONE}), // range must have start value
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "1-", FILE_ONE}), // range must have end value
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "1-5-10", FILE_ONE}), // cannot have multiple hyphens
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "1--5", FILE_ONE}), // cannot have multiple hyphens
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-1-5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-1,5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, ",1", FILE_ONE}), // cannot start with comma
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "1,", FILE_ONE}), // cannot end with comma
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, ",1,", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "1,,5", FILE_ONE}) // cannot have consecutive commas
        );
    }

    static Stream<Arguments> validRangeList() {
        return Stream.of(
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE},
                        List.of(new int[]{1, 1})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, MULTI_NUM, FILE_ONE},
                        List.of(new int[]{1, 1}, new int[]{5, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, RANGE_OF_NUM, FILE_ONE},
                        List.of(new int[]{1, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "1,5,10-15", FILE_ONE},
                        List.of(new int[]{1, 1}, new int[]{5, 5}, new int[]{10, 15})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "10-15,5,1", FILE_ONE},
                        List.of(new int[]{1, 1}, new int[]{5, 5}, new int[]{10, 15})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, "-"},
                        List.of(new int[]{1, 1})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, MULTI_NUM, "-"},
                        List.of(new int[]{1, 1}, new int[]{5, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, RANGE_OF_NUM, "-"},
                        List.of(new int[]{1, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "1,5,10-15", "-"},
                        List.of(new int[]{1, 1}, new int[]{5, 5}, new int[]{10, 15})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "10-15,5,1", "-"},
                        List.of(new int[]{1, 1}, new int[]{5, 5}, new int[]{10, 15}))
        );
    }

    @BeforeEach
    void setUp() {
        this.parser = new CutArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {FLAG_CUT_BY_CHAR, FLAG_CUT_BY_BYTE})
    void parse_ValidFlag_ShouldMatchGivenFlags(String flags) {
        assertDoesNotThrow(() -> parser.parse(flags, SINGLE_NUM, FILE_ONE));

        // Retain only the common elements between parser.flags and VALID_FLAGS
        parser.flags.retainAll(VALID_FLAGS);

        // Check if there's at least one common element
        assertFalse(parser.flags.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-C", "-B", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(args, SINGLE_NUM, FILE_ONE)
        );
        String expected = String.format("illegal option -- %s", args.charAt(1));
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> parser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        // InvalidArgsException to be thrown for scenarios where no flag is provided or more than one flag is given
        assertThrows(InvalidArgsException.class, () -> parser.parse(args));
    }

    @Test
    void parse_NoFlags_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(SINGLE_NUM, FILE_ONE)
        );
        String expected = "You must specify either cut by character or byte";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void parse_BothFlagsSelected_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE)
        );
        String expected = "Only one flag can be selected";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "@", "/", "+"})
    void parse_InvalidRangeCharacter_ThrowsInvalidArgsException(String invalid) {
        String range = String.format("1-5,%s,10-15", invalid);
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, range, FILE_ONE)
        );
        String expected = String.format("invalid byte/character position: '%s'", invalid);
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {",1,5", "1,5,", "1,,5"})
    void parse_EmptyCharacterAtStartOrEndOrInBetweenCommas_ThrowsInvalidArgsException(String range) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, range, FILE_ONE)
        );
        String expected = "byte/character positions are numbered from 1";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0,1", "1,0", "1-5,0", "0,1-5"})
    void parse_SingleCharacterLessThanOne_ThrowsInvalidArgsException(String range) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, range, FILE_ONE)
        );
        String expected = "byte/character positions are numbered from 1";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void parse_HyphenWithoutStartOrEndValues_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, "1-5,-,10-15", FILE_ONE)
        );
        String expected = "invalid range with no endpoint: '-'";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1-3-5", "1-", "-1", "1--5"})
    void parse_InvalidHyphenFormat_ThrowsInvalidArgsException(String invalid) {
        String range = String.format("1-5,%s,10-15", invalid);
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, range, FILE_ONE)
        );
        String expected = String.format("invalid range format: '%s'", invalid);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void parse_HyphenStartValueLessThanOne_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, "0-5", FILE_ONE)
        );
        String expected = "byte/character positions are numbered from 1";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"5-1", "10-5", "15-10"})
    void parse_HyphenEndValueLessThanStartValue_ThrowsInvalidArgsException(String range) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, range, FILE_ONE)
        );
        String expected = String.format("invalid decreasing range: '%s'", range);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void parse_CutValueTooLarge_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, TOO_LARGE_RANGE, FILE_ONE)
        );
        String expected = String.format("byte/character offset '%s' is too large", TOO_LARGE_RANGE);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void isCharPo_ValidFlagAndSyntax_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE));
        assertTrue(parser.isCharPo());
    }

    @Test
    void isCharPo_DifferentValidFlagAndSyntax_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE));
        assertFalse(parser.isCharPo());
    }

    @Test
    void isBytePo_ValidFlagAndSyntax_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE));
        assertTrue(parser.isBytePo());
    }

    @Test
    void isBytePo_DifferentValidFlagAndSyntax_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE));
        assertFalse(parser.isBytePo());
    }

    @ParameterizedTest
    @MethodSource("validRangeList")
    void getRangeList_ValidList_ReturnsSortedList(String[] args, List<int[]> expected) {
        assertDoesNotThrow(() -> parser.parse(args));
        List<int[]> actualList = assertDoesNotThrow(() -> parser.getRangeList());
        assertEquals(expected.size(), actualList.size());
        for (int i = 0; i < actualList.size(); i++) {
            assertArrayEquals(expected.get(i), actualList.get(i));
        }
    }

    @Test
    void getRangeList_CutValueLargerThanMaxInteger_ReturnsMaxInteger() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CUT_BY_CHAR, "5294967296", FILE_ONE));
        List<int[]> actualList = assertDoesNotThrow(() -> parser.getRangeList());
        List<int[]> expected = List.of(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE});
        for (int i = 0; i < actualList.size(); i++) {
            assertArrayEquals(expected.get(i), actualList.get(i));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c 1 example.txt", "-c 1 -"})
    void getFileNames_OneFileGiven_ReturnsOneFile(String args) {
        String[] arguments = args.split("\\s+");
        String lastFile = arguments[arguments.length - 1];
        assertDoesNotThrow(() -> parser.parse(arguments));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(lastFile);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c 1 example1.txt example2.txt", "-c 1 - -", "-c 1 example1.txt -"})
    void getFileNames_MultipleFilesGiven_ReturnsMultipleFiles(String args) {
        String[] arguments = args.split("\\s+");
        String secondLastFile = arguments[arguments.length - 2];
        String lastFile = arguments[arguments.length - 1];
        assertDoesNotThrow(() -> parser.parse(arguments));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(secondLastFile, lastFile);
        assertEquals(expected, result);
    }
}
