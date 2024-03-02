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


public class TeeArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('a');
    private TeeArgsParser teeArgsParser;

    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-a"}),
                Arguments.of((Object) new String[]{"-a", "example.txt"}),
                Arguments.of((Object) new String[]{"example.txt", "-a"}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"example.txt"})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-A"}),
                Arguments.of((Object) new String[]{"--"})
        );
    }

    @BeforeEach
    void setUp() {
        this.teeArgsParser = new TeeArgsParser();
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() {
        assertDoesNotThrow(() -> teeArgsParser.parse("-a"));
        assertEquals(VALID_FLAGS, teeArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-b", "-1", "-!", "-A", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        String expectedMsg = "illegal option -- " + args.charAt(1);
        InvalidArgsException exception = assertThrowsExactly(InvalidArgsException.class, () -> {
            teeArgsParser.parse(args);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> teeArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> teeArgsParser.parse(args));
    }

    @Test
    void isAppend_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> teeArgsParser.parse("-a"));
        assertTrue(teeArgsParser.isAppend());
    }

    @Test
    void isAppend_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> teeArgsParser.parse("example.txt"));
        assertFalse(teeArgsParser.isAppend());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> teeArgsParser.parse());
        List<String> result = teeArgsParser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneNonFlagArg() {
        assertDoesNotThrow(() -> teeArgsParser.parse("example.txt"));
        List<String> result = teeArgsParser.getFileNames();
        List<String> expected = List.of("example");
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArgs_ReturnsMultipleNonFlagArgs() {
        assertDoesNotThrow(() -> teeArgsParser.parse("example1.txt", "example2.txt", "example3.txt"));
        List<String> expected = List.of("example1.txt", "example2.txt", "example3.txt");
        List<String> result = teeArgsParser.getFileNames();
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagAndOneNonFlagArg_ReturnsOneNonFlagArg() {
        assertDoesNotThrow(() -> teeArgsParser.parse("-a", "example.txt"));
        List<String> expected = List.of("example.txt");
        List<String> result = teeArgsParser.getFileNames();
        assertEquals(expected, result);
    }
}