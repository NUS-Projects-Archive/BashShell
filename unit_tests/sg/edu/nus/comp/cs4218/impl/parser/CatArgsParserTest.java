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

class CatArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('n');
    private static final String FILE = "file";
    private CatArgsParser parser;

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
        parser = new CatArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.flags.isEmpty());
        assertTrue(parser.nonFlagArgs.contains(args));
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() {
        assertDoesNotThrow(() -> parser.parse("-n"));
        assertEquals(VALID_FLAGS, parser.flags);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-P", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () -> parser.parse(args));
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> parser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> parser.parse(args));
    }

    @Test
    void isLineNumber_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse());
        assertFalse(parser.isLineNumber());
    }

    @Test
    void isLineNumber_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse("-n"));
        assertTrue(parser.isLineNumber());
    }

    @Test
    void isLineNumber_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse("-n", FILE));
        assertTrue(parser.isLineNumber());
    }

    @Test
    void isLineNumber_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE));
        assertFalse(parser.isLineNumber());
    }

    @Test
    void getFiles_NoArgs_ReturnsEmpty() {
        assertDoesNotThrow(() -> parser.parse());
        List<String> result = parser.getFiles();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFiles_OneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> parser.parse(FILE));
        List<String> result = parser.getFiles();
        List<String> expected = List.of(FILE);
        assertEquals(expected, result);
    }

    @Test
    void getFiles_MultipleNonFlagArgs_ReturnsMultipleFiles() {
        assertDoesNotThrow(() -> parser.parse("file1.txt", "file2.txt", "file3.txt"));
        List<String> result = parser.getFiles();
        List<String> expected = List.of("file1.txt", "file2.txt", "file3.txt");
        assertEquals(expected, result);
    }

    @Test
    void getFiles_ValidFlagAndOneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> parser.parse("-n", FILE));
        List<String> result = parser.getFiles();
        List<String> expected = List.of(FILE);
        assertEquals(expected, result);
    }
}
