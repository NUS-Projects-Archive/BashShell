package sg.edu.nus.comp.cs4218.impl.parser;

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
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String STDIN = "-";
    private CutArgsParser parser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, MULTI_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, MULTI_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, RANGE_OF_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, RANGE_OF_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, MULTI_AND_RANGE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, MULTI_AND_RANGE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, MULTI_AND_RANGE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, MULTI_AND_RANGE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, STDIN, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, STDIN, STDIN}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE, FILE_TWO})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{SINGLE_NUM, FILE_ONE}), // lacking flag
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, FILE_ONE}), // lacking range
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, FILE_ONE}), // lacking range
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, FLAG_CUT_BY_BYTE, SINGLE_NUM, FILE_ONE}), // only 1 flag
                Arguments.of((Object) new String[]{FLAG_CUT_BY_BYTE, FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE}), // only 1 flag
                Arguments.of((Object) new String[]{"-C", SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{"-B", SINGLE_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "0", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "0-5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "0,5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-1", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-1-5", FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CUT_BY_CHAR, "-1,5", FILE_ONE})
        );
    }

    static Stream<Arguments> validRangeList() {
        return Stream.of(
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, FILE_ONE},
                        List.of(new int[]{1, 1})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, MULTI_NUM, FILE_ONE},
                        List.of(new int[]{1, 1}, new int[]{1, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, RANGE_OF_NUM, FILE_ONE},
                        List.of(new int[]{1, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "1,5,10-15", FILE_ONE},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15}),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "10-15,5,1", FILE_ONE},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15}),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, SINGLE_NUM, "-"},
                        List.of(new int[]{1, 1})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, MULTI_NUM, "-"},
                        List.of(new int[]{1, 1}, new int[]{1, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, RANGE_OF_NUM, "-"},
                        List.of(new int[]{1, 5})),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "1,5,10-15", "-"},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15}),
                Arguments.of(new String[]{FLAG_CUT_BY_CHAR, "10-15,5,1", "-"},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15})
        );
    }

    @BeforeEach
    void setUp() {
        this.parser = new CutArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.flags.isEmpty());
        assertTrue(parser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {FLAG_CUT_BY_CHAR, FLAG_CUT_BY_BYTE})
    void parse_ValidFlag_ShouldMatchGivenFlags(String args) {
        assertDoesNotThrow(() -> parser.parse(args));

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

    @Test
    void parse_NoFlags_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(SINGLE_NUM, FILE_ONE)
        );
        String expected = "Invalid syntax"; // one valid flag is expected
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
    void getRangeList_ValidList_ReturnsSortedList(String[] args, List<Integer[]> expected) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertEquals(parser.getRangeList(), expected);
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
