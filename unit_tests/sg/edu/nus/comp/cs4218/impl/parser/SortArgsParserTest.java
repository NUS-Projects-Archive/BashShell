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
    private SortArgsParser parser;

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
        parser = new SortArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.flags.isEmpty());
        assertTrue(parser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE})
    void parse_ValidFlag_ShouldMatchGivenFlags(String args) {
        assertDoesNotThrow(() -> parser.parse(args));

        // Retain only the common elements between parser.flags and VALID_FLAGS
        parser.flags.retainAll(VALID_FLAGS);

        // Check if there's at least one common element
        assertFalse(parser.flags.isEmpty());
    }

    @Test
    void parse_AllValidFlags_ReturnsGivenMatchingFlags() {
        assertDoesNotThrow(() -> parser.parse(FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE));
        assertEquals(VALID_FLAGS, parser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-nrf", "-nfr", "-rnf", "-rfn", "-fnr", "-frn"})
    void parse_AllValidFlagsTogether_ReturnsGivenMatchingFlags(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertEquals(VALID_FLAGS, parser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-N", "-R", "-F", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () -> parser.parse(args));
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> parser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> parser.parse(args));
    }

    @Test
    void isFirstWordNumber_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse());
        assertFalse(parser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_FIRST_NUM));
        assertTrue(parser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_FIRST_NUM, FILE_ONE));
        assertTrue(parser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertFalse(parser.isFirstWordNumber());
    }

    @Test
    void isReverseOrder_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertFalse(parser.isReverseOrder());
    }

    @Test
    void isReverseOrder_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_REV_ORDER));
        assertTrue(parser.isReverseOrder());
    }

    @Test
    void isReverseOrder_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_REV_ORDER, FILE_ONE));
        assertTrue(parser.isReverseOrder());
    }

    @Test
    void isReverseOrder_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertFalse(parser.isReverseOrder());
    }

    @Test
    void isCaseIndependent_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertFalse(parser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CASE_IGNORE));
        assertTrue(parser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CASE_IGNORE, FILE_ONE));
        assertTrue(parser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertFalse(parser.isCaseIndependent());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> parser.parse());
        List<String> result = parser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArg_ReturnsMultipleFiles() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> parser.parse(FLAG_FIRST_NUM, FLAG_REV_ORDER, FLAG_CASE_IGNORE, FILE_ONE));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }
}
