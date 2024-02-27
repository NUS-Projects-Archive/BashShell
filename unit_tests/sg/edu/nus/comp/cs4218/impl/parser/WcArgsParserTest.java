package sg.edu.nus.comp.cs4218.impl.parser;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

public class WcArgsParserTest {
    private final Set<Character> VALID_FLAGS = Set.of('c', 'l', 'w');
    private WcArgsParser wcArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(Arguments.of((Object) new String[]{}), Arguments.of((Object) new String[]{"-c"}), Arguments.of((Object) new String[]{"-c", "-l"}), Arguments.of((Object) new String[]{"-c", "-l", "w"}), Arguments.of((Object) new String[]{"example"}), Arguments.of((Object) new String[]{"-c", "example"}), Arguments.of((Object) new String[]{"-c", "-l", "example"}), Arguments.of((Object) new String[]{"example", "-l", "-w"}));
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(Arguments.of((Object) new String[]{"-C"}), Arguments.of((Object) new String[]{"-C", "-L"}), Arguments.of((Object) new String[]{"-W"}), Arguments.of((Object) new String[]{"--"}), Arguments.of((Object) new String[]{"-a"}));
    }

    @BeforeEach
    void setUp() {
        this.wcArgsParser = new WcArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) throws InvalidArgsException {
        wcArgsParser.parse(args);
        assertTrue(wcArgsParser.flags.isEmpty());
        assertTrue(wcArgsParser.nonFlagArgs.contains(args));
    }

    @Test
    void parse_ValidFlags_ReturnsGivenMatchingFlags() throws InvalidArgsException {
        wcArgsParser.parse("-c", "-w", "-l");
        assertEquals(VALID_FLAGS, wcArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-C", "-W", "-L", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
            wcArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> wcArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse(args));
    }

    @Test
    void hasNoFlags_NoFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse();
        assertTrue(wcArgsParser.hasNoFlags());
    }


    @Test
    void isByteCount_NoFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse();
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlagsExcludingByteFlag_ReturnsFalse() throws InvalidArgsException {
        wcArgsParser.parse("-w", "-l");
        assertFalse(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("-c");
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("-c", "example");
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_OnlyNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("example");
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isLineCount_NoFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse();
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlagsExcludingLineFlag_ReturnsFalse() throws InvalidArgsException {
        wcArgsParser.parse("-w", "-c");
        assertFalse(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("-l");
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("-l", "example");
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_OnlyNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("example");
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isWordCount_NoFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse();
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlagsExcludingWordFlag_ReturnsFalse() throws InvalidArgsException {
        wcArgsParser.parse("-c", "-l");
        assertFalse(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("-w");
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("-w", "example");
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_OnlyNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse("example");
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() throws InvalidArgsException {
        wcArgsParser.parse();
        List<String> result = wcArgsParser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneFileName() throws InvalidArgsException {
        wcArgsParser.parse("example");
        List<String> result = wcArgsParser.getFileNames();
        List<String> expected = List.of("example");
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArgs_ReturnsMultipleFileNames() throws InvalidArgsException {
        wcArgsParser.parse("example1", "example2", "example3");
        List<String> result = wcArgsParser.getFileNames();
        List<String> expected = List.of("example1", "example2", "example3");
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNoFlagArg_ReturnsOneFolder() throws InvalidArgsException {
        wcArgsParser.parse("-w", "-c", "-l", "example");
        List<String> result = wcArgsParser.getFileNames();
        List<String> expected = List.of("example");
        assertEquals(expected, result);
    }

    @Test
    void isStdinOnly_NoArg_ReturnsTrue() throws InvalidArgsException {
        wcArgsParser.parse();
        assertTrue(wcArgsParser.isStdinOnly());
    }

    @Test
    void isStdinOnly_OneNonFlagArg_ReturnsFalse() throws InvalidArgsException {
        wcArgsParser.parse("example");
        assertFalse(wcArgsParser.isStdinOnly());
    }
}
