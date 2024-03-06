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
    private WcArgsParser wcArgsParser;

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
        wcArgsParser = new WcArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> wcArgsParser.parse(args));
        assertTrue(wcArgsParser.flags.isEmpty());
        assertTrue(wcArgsParser.nonFlagArgs.contains(args));
    }

    @Test
    void parse_ValidFlags_ReturnsGivenMatchingFlags() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_BYTE_COUNT, FLAG_WORD_COUNT, FLAG_LINE_COUNT));
        assertEquals(VALID_FLAGS, wcArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-C", "-W", "-L", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () -> {
            wcArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoesNotThrowException(String... args) {
        assertDoesNotThrow(() -> wcArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> wcArgsParser.parse(args));
    }

    @Test
    void hasNoFlags_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse());
        assertTrue(wcArgsParser.hasNoFlags());
    }

    @Test
    void isByteCount_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse());
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlagsExcludingByteFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_WORD_COUNT, FLAG_LINE_COUNT));
        assertFalse(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_BYTE_COUNT));
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_BYTE_COUNT, FILE_ONE));
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isByteCount_OnlyNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FILE_ONE));
        assertTrue(wcArgsParser.isByteCount());
    }

    @Test
    void isLineCount_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse());
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlagsExcludingLineFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_WORD_COUNT, FLAG_BYTE_COUNT));
        assertFalse(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_LINE_COUNT));
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_LINE_COUNT, FILE_ONE));
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isLineCount_OnlyNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FILE_ONE));
        assertTrue(wcArgsParser.isLineCount());
    }

    @Test
    void isWordCount_NoFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse());
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlagsExcludingWordFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_BYTE_COUNT, FLAG_LINE_COUNT));
        assertFalse(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_WORD_COUNT));
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_WORD_COUNT, FILE_ONE));
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void isWordCount_OnlyNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FILE_ONE));
        assertTrue(wcArgsParser.isWordCount());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> wcArgsParser.parse());
        List<String> result = wcArgsParser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneFileName() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FILE_ONE));
        List<String> result = wcArgsParser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArgs_ReturnsMultipleFileNames() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = wcArgsParser.getFileNames();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNoFlagArg_ReturnsOneFolder() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FLAG_WORD_COUNT, FLAG_BYTE_COUNT, FLAG_LINE_COUNT, FILE_ONE));
        List<String> result = wcArgsParser.getFileNames();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void isStdinOnly_NoArg_ReturnsTrue() {
        assertDoesNotThrow(() -> wcArgsParser.parse());
        assertTrue(wcArgsParser.isStdinOnly());
    }

    @Test
    void isStdinOnly_OneNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> wcArgsParser.parse(FILE_ONE));
        assertFalse(wcArgsParser.isStdinOnly());
    }
}
