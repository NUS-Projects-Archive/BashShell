package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class CatArgsParserTest {

    private static final String NON_FLAG_ARG = "file";
    private final Set<Character> VALID_FLAGS = Set.of('n');
    private CatArgsParser catArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{"file.txt"}),
                Arguments.of((Object) new String[]{"-n", "file.txt"}),
                Arguments.of((Object) new String[]{"file.txt", "-n"})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-N"}),
                Arguments.of((Object) new String[]{"--"})
        );
    }

    @BeforeEach
    void setUp() {
        catArgsParser = new CatArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        try {
            catArgsParser.parse(args);
            assertTrue(catArgsParser.flags.isEmpty());
            assertTrue(catArgsParser.nonFlagArgs.contains(args));
        } catch (InvalidArgsException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() {
        assertDoesNotThrow(() -> catArgsParser.parse("-n"));
        assertEquals(VALID_FLAGS, catArgsParser.flags);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-P", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
            catArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> catArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> catArgsParser.parse(args));
    }

    @Test
    void isLineNumber_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> catArgsParser.parse());
        assertFalse(catArgsParser.isLineNumber());
    }

    @Test
    void isLineNumber_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> catArgsParser.parse("-n"));
        assertTrue(catArgsParser.isLineNumber());
    }

    @Test
    void isLineNumber_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> catArgsParser.parse("-n", NON_FLAG_ARG));
        assertTrue(catArgsParser.isLineNumber());
    }

    @Test
    void isLineNumber_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> catArgsParser.parse(NON_FLAG_ARG));
        assertFalse(catArgsParser.isLineNumber());
    }

    @Test
    void getNonFlagArgs_NoArgs_ReturnsEmpty() {
        assertDoesNotThrow(() -> catArgsParser.parse());
        List<String> result = catArgsParser.getNonFlagArgs();
        assertTrue(result.isEmpty());
    }

    @Test
    void getNonFlagArgs_OneNonFlagArg_ReturnsOneArg() {
        assertDoesNotThrow(() -> catArgsParser.parse(NON_FLAG_ARG));
        List<String> expected = List.of(NON_FLAG_ARG);
        List<String> result = catArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_MultipleNonFlagArgs_ReturnsMultipleArgs() {
        assertDoesNotThrow(() -> catArgsParser.parse("file1.txt", "file2.txt", "file3.txt"));
        List<String> expected = List.of("file1.txt", "file2.txt", "file3.txt");
        List<String> result = catArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_ValidFlagAndOneNonFlagArg_ReturnsOneArg() {
        assertDoesNotThrow(() -> catArgsParser.parse("-n", NON_FLAG_ARG));
        List<String> expected = List.of(NON_FLAG_ARG);
        List<String> result = catArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
    }
}