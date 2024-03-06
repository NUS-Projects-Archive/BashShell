package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
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

class LsArgsParserTest {

    private static final String FLAG_R = "-R";
    private static final String FLAG_X = "-X";
    private static final String FLAG_XR = "-XR";
    private static final String FLAG_RX = "-RX";
    private static final String FLAG_P = "-P";
    private static final String EXAMPLE = "example";

    private LsArgsParser parser;

    private static Stream<Arguments> validSyntax() {

        final Set<Character> emptyValidFlags = new HashSet<>();
        final List<String> emptyNonFlagArgs = new ArrayList<>();
        final Set<Character> validRFlag = Set.of('R');
        final Set<Character> validXFlag = Set.of('X');
        final Set<Character> validFlags = Set.of('R', 'X');
        final List<String> nonFlagArgs = List.of(EXAMPLE);

        return Stream.of(
                Arguments.of(new String[]{}, emptyValidFlags, emptyNonFlagArgs),
                Arguments.of(new String[]{FLAG_R}, validRFlag, emptyNonFlagArgs),
                Arguments.of(new String[]{FLAG_X}, validXFlag, emptyNonFlagArgs),
                Arguments.of(new String[]{FLAG_R, FLAG_X}, validFlags, emptyNonFlagArgs),
                Arguments.of(new String[]{FLAG_RX}, validFlags, emptyNonFlagArgs),
                Arguments.of(new String[]{FLAG_XR}, validFlags, emptyNonFlagArgs),
                Arguments.of(new String[]{EXAMPLE}, emptyValidFlags, nonFlagArgs),
                Arguments.of(new String[]{EXAMPLE, FLAG_R}, validRFlag, nonFlagArgs),
                Arguments.of(new String[]{EXAMPLE, FLAG_X}, validXFlag, nonFlagArgs),
                Arguments.of(new String[]{EXAMPLE, FLAG_R, FLAG_X}, validFlags, nonFlagArgs),
                Arguments.of(new String[]{EXAMPLE, FLAG_RX}, validFlags, nonFlagArgs),
                Arguments.of(new String[]{EXAMPLE, FLAG_XR}, validFlags, nonFlagArgs));
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{FLAG_P}),
                Arguments.of((Object) new String[]{"--"}),
                Arguments.of((Object) new String[]{"-r", FLAG_X}),
                Arguments.of((Object) new String[]{FLAG_R, FLAG_X, FLAG_P}));
    }

    private void testGetDirectoriesReturningEmpty(String... args) {
        // When
        assertDoesNotThrow(() -> parser.parse(args));
        List<String> result = parser.getDirectories();

        // Then
        assertTrue(result.isEmpty());
    }

    @BeforeEach
    void setUp() {
        parser = new LsArgsParser();
    }

    /**
     * Tests if parse sets the flags and non-flag arguments correctly.
     *
     * @param args          Arguments to parse
     * @param expectedFlags Expected flags
     * @param expectedNFA   Expected non-flag arguments
     */
    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_HasCorrectFlagsAndNonFlagArgs(String[] args, Set<Character> expectedFlags,
                                                         List<String> expectedNFA) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertEquals(expectedFlags, parser.flags, "Flags do not match");
        assertEquals(expectedNFA, parser.nonFlagArgs, "Non-flag arguments do not match");
    }

    /**
     * Tests if parse throws InvalidArgsException when given invalid syntax.
     *
     * @param args Arguments to parse
     */
    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> parser.parse(args));
    }

    /**
     * Tests if isRecursive returns true when -R flag is present in args.
     *
     * @param args Arguments with -R to parse
     */
    @ParameterizedTest
    @ValueSource(strings = {FLAG_R, FLAG_RX, FLAG_XR})
    void isRecursive_ValidFlag_ReturnsTrue(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.isRecursive());
    }

    /**
     * Tests if isRecursive returns false when -R flag is not present in args.
     *
     * @param args Arguments without -R to parse
     */
    @ParameterizedTest
    @ValueSource(strings = {FLAG_X, EXAMPLE})
    void isRecursive_InvalidFlag_ReturnsFalse(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertFalse(parser.isRecursive());
    }

    /**
     * Tests if isRecursive returns false when there are no flags.
     */
    @Test
    void isRecursive_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(""));
        assertFalse(parser.isRecursive());
    }

    /**
     * Tests if isSortByExt returns true when -X flag is present in args.
     *
     * @param args Arguments with -X to parse
     */
    @ParameterizedTest
    @ValueSource(strings = {FLAG_X, FLAG_RX, FLAG_XR})
    void isSortByExt_ValidFlag_ReturnsTrue(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.isSortByExt());
    }

    /**
     * Tests if isSortByExt returns false when -X flag is not present in args.
     *
     * @param args Arguments without -X to parse
     */
    @ParameterizedTest
    @ValueSource(strings = {FLAG_R, EXAMPLE})
    void isSortByExt_InvalidFlag_ReturnsFalse(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertFalse(parser.isSortByExt());
    }

    /**
     * Tests if isSortByExt returns false when there are no flags.
     */
    @Test
    void isSortByExt_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(""));
        assertFalse(parser.isSortByExt());
    }

    /**
     * Tests if getDirectories returns empty list when there are no arguments.
     */
    @Test
    void getDirectories_NoArg_ReturnsEmpty() {
        testGetDirectoriesReturningEmpty();
    }

    /**
     * Tests if getDirectories returns empty list when there are no non-flag arguments.
     */
    @Test
    void getDirectories_WithArgsThatHasNoNonFlagArgs_ReturnsEmpty() {
        testGetDirectoriesReturningEmpty(FLAG_R, FLAG_X);
    }

    /**
     * Tests if getDirectories returns non-flag arguments as its directories.
     */
    @Test
    void getDirectories_WithArgsThatHasNonFlagArgs_ReturnsNonFlagArgs() {
        // Given
        String[] args = new String[]{EXAMPLE, FLAG_R, FLAG_X};

        // When
        assertDoesNotThrow(() -> parser.parse(args));
        List<String> result = parser.getDirectories();

        // Then
        List<String> expected = List.of(EXAMPLE);
        assertEquals(expected, result);
    }
}
