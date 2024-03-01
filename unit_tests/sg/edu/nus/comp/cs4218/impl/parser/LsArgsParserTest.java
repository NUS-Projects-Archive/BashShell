package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

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

import static org.junit.jupiter.api.Assertions.*;

class LsArgsParserTest {

    private static final String FLAG_R = "-R";
    private static final String FLAG_X = "-X";
    private static final String FLAG_XR = "-XR";
    private static final String FLAG_RX = "-RX";
    private static final String FLAG_P = "-P";
    private static final String EXAMPLE = "example";

    private LsArgsParser lsArgsParser;

    private static Stream<Arguments> validSyntax() {

        final Set<Character> emptyValidFlags = new HashSet<>();
        final List<String> emptyNonFlagArgs = new ArrayList<>();
        final Set<Character> validRFlag = Set.of('R');
        final Set<Character> validXFlag = Set.of('X');
        final Set<Character> validFlags = Set.of('R', 'X');
        final List<String> nonFlagArgs = List.of(EXAMPLE);

        return Stream.of(
                Arguments.of(new String[] {}, emptyValidFlags, emptyNonFlagArgs),
                Arguments.of(new String[] { FLAG_R }, validRFlag, emptyNonFlagArgs),
                Arguments.of(new String[] { FLAG_X }, validXFlag, emptyNonFlagArgs),
                Arguments.of(new String[] { FLAG_R, FLAG_X }, validFlags, emptyNonFlagArgs),
                Arguments.of(new String[] { FLAG_RX }, validFlags, emptyNonFlagArgs),
                Arguments.of(new String[] { FLAG_XR }, validFlags, emptyNonFlagArgs),
                Arguments.of(new String[] { EXAMPLE }, emptyValidFlags, nonFlagArgs),
                Arguments.of(new String[] { EXAMPLE, FLAG_R }, validRFlag, nonFlagArgs),
                Arguments.of(new String[] { EXAMPLE, FLAG_X }, validXFlag, nonFlagArgs),
                Arguments.of(new String[] { EXAMPLE, FLAG_R, FLAG_X }, validFlags, nonFlagArgs),
                Arguments.of(new String[] { EXAMPLE, FLAG_RX }, validFlags, nonFlagArgs),
                Arguments.of(new String[] { EXAMPLE, FLAG_XR }, validFlags, nonFlagArgs));
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[] { FLAG_P }),
                Arguments.of((Object) new String[] { "--" }),
                Arguments.of((Object) new String[] { "-r", FLAG_X }),
                Arguments.of((Object) new String[] { FLAG_R, FLAG_X, FLAG_P }));
    }

    @BeforeEach
    void setUp() {
        this.lsArgsParser = new LsArgsParser();
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_HasCorrectFlagsAndNonFlagArgs(String[] args, Set<Character> expectedFlags,
                                                         List<String> expectedNFA) {
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        assertEquals(expectedFlags, lsArgsParser.flags, "Flags do not match");
        assertEquals(expectedNFA, lsArgsParser.nonFlagArgs, "Non-flag arguments do not match");
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> lsArgsParser.parse(args));
    }

    @ParameterizedTest
    @ValueSource(strings = { FLAG_R, FLAG_RX, FLAG_XR })
    void isRecursive_ValidFlag_ReturnsTrue(String args) {
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        assertTrue(lsArgsParser.isRecursive());
    }

    @ParameterizedTest
    @ValueSource(strings = { FLAG_X, EXAMPLE })
    void isRecursive_InvalidFlag_ReturnsFalse(String args) {
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        assertFalse(lsArgsParser.isRecursive());
    }

    @Test
    void isRecursive_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> lsArgsParser.parse(""));
        assertFalse(lsArgsParser.isRecursive());
    }

    @ParameterizedTest
    @ValueSource(strings = { FLAG_X, FLAG_RX, FLAG_XR })
    void isSortByExt_ValidFlag_ReturnsTrue(String args) {
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        assertTrue(lsArgsParser.isSortByExt());
    }

    @ParameterizedTest
    @ValueSource(strings = { FLAG_R, EXAMPLE })
    void isSortByExt_InvalidFlag_ReturnsFalse(String args) {
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        assertFalse(lsArgsParser.isSortByExt());
    }

    @Test
    void isSortByExt_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> lsArgsParser.parse(""));
        assertFalse(lsArgsParser.isSortByExt());
    }

    private void testGetDirectoriesReturningEmpty(String... args) {
        // When
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        List<String> result = lsArgsParser.getDirectories();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getDirectories_NoArg_ReturnsEmpty() {
        testGetDirectoriesReturningEmpty();
    }

    @Test
    void getDirectories_WithArgsThatHasNoNonFlagArgs_ReturnsEmpty() {
        testGetDirectoriesReturningEmpty(FLAG_R, FLAG_X);
    }

    @Test
    void getDirectories_WithArgsThatHasNonFlagArgs_ReturnsNonFlagArgs() {
        // Given
        String[] args = new String[] { EXAMPLE, FLAG_R, FLAG_X };
        List<String> expected = List.of(EXAMPLE);

        // When
        assertDoesNotThrow(() -> lsArgsParser.parse(args));
        List<String> result = lsArgsParser.getDirectories();

        // Then
        assertEquals(expected, result);
    }
}