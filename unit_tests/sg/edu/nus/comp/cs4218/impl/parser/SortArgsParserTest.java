package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

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

class SortArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('n', 'r', 'f');
    private static final String FLAG_FIRST_NUM = "-n";
    private static final String FLAG_REV_ORDER = "-r";
    private static final String FLAG_CASE_IGNORE = "-f";
    private static final String FLAG_ALL = "-nrf";
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";
    private static final String DASH = "-";
    private SortArgsParser sortArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{FLAG_FIRST_NUM}),
                Arguments.of((Object) new String[]{FLAG_REV_ORDER}),
                Arguments.of((Object) new String[]{FLAG_CASE_IGNORE}),
                Arguments.of((Object) new String[]{FLAG_FIRST_NUM, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_REV_ORDER, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CASE_IGNORE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_FIRST_NUM, DASH}), // dash is treated as a file
                Arguments.of((Object) new String[]{FLAG_REV_ORDER, DASH}),
                Arguments.of((Object) new String[]{FLAG_CASE_IGNORE, DASH}),
                Arguments.of((Object) new String[]{FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE}),
                Arguments.of((Object) new String[]{FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE, DASH}),
                Arguments.of((Object) new String[]{FLAG_ALL}),
                Arguments.of((Object) new String[]{FLAG_ALL, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_ALL, DASH})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-n", "-a", "example"}),
                Arguments.of((Object) new String[]{"-n", "--", "example"}),
                Arguments.of((Object) new String[]{"-N"}),
                Arguments.of((Object) new String[]{"-R"}),
                Arguments.of((Object) new String[]{"-F"})
        );
    }

    @BeforeEach
    void setUp() {
        sortArgsParser = new SortArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> sortArgsParser.parse(args));
        assertTrue(sortArgsParser.flags.isEmpty());
        assertTrue(sortArgsParser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE})
    void parse_ValidFlag_ShouldMatchGivenFlags(String args) {
        assertDoesNotThrow(() -> sortArgsParser.parse(args));

        // Retain only the common elements between sortArgsParser.flags and VALID_FLAGS
        sortArgsParser.flags.retainAll(VALID_FLAGS);

        // Check if there's at least one common element
        assertFalse(sortArgsParser.flags.isEmpty());
    }

    @Test
    void parse_AllValidFlags_ReturnsGivenMatchingFlags() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE));
        assertEquals(VALID_FLAGS, sortArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-nrf", "-nfr", "-rnf", "-rfn", "-fnr", "-frn"})
    void parse_AllValidFlagsTogether_ReturnsGivenMatchingFlags(String args) {
        assertDoesNotThrow(() -> sortArgsParser.parse(args));
        assertEquals(VALID_FLAGS, sortArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-N", "-R", "-F", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () -> {
            sortArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> sortArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> sortArgsParser.parse(args));
    }

    @Test
    void isFirstWordNumber_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse());
        assertFalse(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_FIRST_NUM));
        assertTrue(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_FIRST_NUM, FILE_ONE));
        assertTrue(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FILE_ONE));
        assertFalse(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isReverseOrder_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse());
        assertFalse(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_REV_ORDER));
        assertTrue(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_REV_ORDER, FILE_ONE));
        assertTrue(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FILE_ONE));
        assertFalse(sortArgsParser.isReverseOrder());
    }

    @Test
    void isCaseIndependent_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse());
        assertFalse(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_CASE_IGNORE));
        assertTrue(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_CASE_IGNORE, FILE_ONE));
        assertTrue(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FILE_ONE));
        assertFalse(sortArgsParser.isCaseIndependent());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> sortArgsParser.parse());
        List<String> result = sortArgsParser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FILE_ONE));
        List<String> result = sortArgsParser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArg_ReturnsMultipleFiles() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = sortArgsParser.getFileNames();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> sortArgsParser.parse(FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE, FILE_ONE));
        List<String> result = sortArgsParser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }
}
