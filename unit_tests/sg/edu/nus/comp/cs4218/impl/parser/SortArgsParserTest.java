package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class SortArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('n', 'r', 'f');
    private SortArgsParser sortArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{"-r"}),
                Arguments.of((Object) new String[]{"-f"}),
                Arguments.of((Object) new String[]{"-n", "example"}),
                Arguments.of((Object) new String[]{"-r", "example"}),
                Arguments.of((Object) new String[]{"-f", "example"}),
                Arguments.of((Object) new String[]{"-n", "-"}),
                Arguments.of((Object) new String[]{"-r", "-"}),
                Arguments.of((Object) new String[]{"-f", "-"}),
                Arguments.of((Object) new String[]{"-n", "-r", "-f"}),
                Arguments.of((Object) new String[]{"-n", "-r", "-f", "example"}),
                Arguments.of((Object) new String[]{"-n", "-r", "-f", "-"}), // dash is treated as a file
                Arguments.of((Object) new String[]{"-nrf"}),
                Arguments.of((Object) new String[]{"-nrf", "example"}),
                Arguments.of((Object) new String[]{"-nrf", "-"})
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
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args)
            throws InvalidArgsException {
        sortArgsParser.parse(args);
        assertTrue(sortArgsParser.flags.isEmpty());
        assertTrue(sortArgsParser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-n", "-r", "-f"})
    void parse_ValidFlag_ShouldMatchGivenFlags(String args) {
        assertDoesNotThrow(() -> sortArgsParser.parse(args));

        // Retain only the common elements between sortArgsParser.flags and VALID_FLAGS
        sortArgsParser.flags.retainAll(VALID_FLAGS);

        // Check if there's at least one common element
        assertFalse(sortArgsParser.flags.isEmpty());
    }

    @Test
    void parse_AllValidFlags_ReturnsGivenMatchingFlags() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-n", "-r", "-f"));
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
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
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
        assertDoesNotThrow(() -> sortArgsParser.parse("-n"));
        assertTrue(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-n", "example"));
        assertTrue(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isFirstWordNumber_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse("example"));
        assertFalse(sortArgsParser.isFirstWordNumber());
    }

    @Test
    void isReverseOrder_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse());
        assertFalse(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-r"));
        assertTrue(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-r", "example"));
        assertTrue(sortArgsParser.isReverseOrder());
    }

    @Test
    void isReverseOrder_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse("example"));
        assertFalse(sortArgsParser.isReverseOrder());
    }

    @Test
    void isCaseIndependent_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse());
        assertFalse(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-f"));
        assertTrue(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-f", "example"));
        assertTrue(sortArgsParser.isCaseIndependent());
    }

    @Test
    void isCaseIndependent_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> sortArgsParser.parse("example"));
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
        assertDoesNotThrow(() -> sortArgsParser.parse("example"));
        List<String> expected = List.of("example");
        List<String> result = sortArgsParser.getFileNames();
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArg_ReturnsMultipleFiles() {
        assertDoesNotThrow(() -> sortArgsParser.parse("example1", "example2", "example3"));
        List<String> expected = List.of("example1", "example2", "example3");
        List<String> result = sortArgsParser.getFileNames();
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> sortArgsParser.parse("-n", "-r", "-f", "example"));
        List<String> expected = List.of("example");
        List<String> result = sortArgsParser.getFileNames();
        assertEquals(expected, result);
    }
}