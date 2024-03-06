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

public class WcArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('c', 'l', 'w');
    public static final String FLAG_BYTE_COUNT = "-c";

    public static final String FLAG_LINE_COUNT = "-l";

    public static final String FLAG_WORD_COUNT = "-w";
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";
    private WcArgsParser parser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{FLAG_BYTE_COUNT}),
                Arguments.of((Object) new String[]{FLAG_BYTE_COUNT, FLAG_LINE_COUNT}),
                Arguments.of((Object) new String[]{FLAG_BYTE_COUNT, FLAG_LINE_COUNT, FLAG_WORD_COUNT}),
                Arguments.of((Object) new String[]{FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_BYTE_COUNT, FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_BYTE_COUNT, FLAG_LINE_COUNT, FILE_ONE}),
                Arguments.of((Object) new String[]{FILE_ONE, FLAG_LINE_COUNT, FLAG_WORD_COUNT}));
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-C"}),
                Arguments.of((Object) new String[]{"-C", "-L"}),
                Arguments.of((Object) new String[]{"-W"}),
                Arguments.of((Object) new String[]{"--"}),
                Arguments.of((Object) new String[]{"-a"}));
    }

    @BeforeEach
    void setUp() {
        parser = new WcArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.flags.isEmpty());
        assertTrue(parser.nonFlagArgs.contains(args));
    }

    @Test
    void parse_ValidFlags_ReturnsGivenMatchingFlags() {
        assertDoesNotThrow(() -> parser.parse(FLAG_BYTE_COUNT, FLAG_WORD_COUNT, FLAG_LINE_COUNT));
        assertEquals(VALID_FLAGS, parser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-C", "-W", "-L", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () -> parser.parse(args));
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoesNotThrowException(String... args) {
        assertDoesNotThrow(() -> parser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> parser.parse(args));
    }

    @Test
    void hasNoFlags_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertTrue(parser.hasNoFlags());
    }

    @Test
    void isByteCount_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertTrue(parser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlagsExcludingByteFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FLAG_WORD_COUNT, FLAG_LINE_COUNT));
        assertFalse(parser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_BYTE_COUNT));
        assertTrue(parser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_BYTE_COUNT, FILE_ONE));
        assertTrue(parser.isByteCount());
    }

    @Test
    void isByteCount_OnlyNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertTrue(parser.isByteCount());
    }

    @Test
    void isLineCount_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertTrue(parser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlagsExcludingLineFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FLAG_WORD_COUNT, FLAG_BYTE_COUNT));
        assertFalse(parser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_LINE_COUNT));
        assertTrue(parser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_LINE_COUNT, FILE_ONE));
        assertTrue(parser.isLineCount());
    }

    @Test
    void isLineCount_OnlyNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertTrue(parser.isLineCount());
    }

    @Test
    void isWordCount_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertTrue(parser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlagsExcludingWordFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FLAG_BYTE_COUNT, FLAG_LINE_COUNT));
        assertFalse(parser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_WORD_COUNT));
        assertTrue(parser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_WORD_COUNT, FILE_ONE));
        assertTrue(parser.isWordCount());
    }

    @Test
    void isWordCount_OnlyNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertTrue(parser.isWordCount());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> parser.parse());
        List<String> result = parser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneFileName() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArgs_ReturnsMultipleFileNames() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNoFlagArg_ReturnsOneFolder() {
        assertDoesNotThrow(() -> parser.parse(FLAG_WORD_COUNT, FLAG_BYTE_COUNT, FLAG_LINE_COUNT, FILE_ONE));
        List<String> result = parser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void isStdinOnly_NoArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse());
        assertTrue(parser.isStdinOnly());
    }

    @Test
    void isStdinOnly_OneNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertFalse(parser.isStdinOnly());
    }
}
