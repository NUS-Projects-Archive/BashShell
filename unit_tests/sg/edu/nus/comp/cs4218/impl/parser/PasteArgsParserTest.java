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

public class PasteArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('s');
    private static final String FLAG_SERIAL = "-s";
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";
    private static final String STDIN = "-";
    private PasteArgsParser parser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{FLAG_SERIAL}),
                Arguments.of((Object) new String[]{FILE_ONE}),
                Arguments.of((Object) new String[]{STDIN}),
                Arguments.of((Object) new String[]{FLAG_SERIAL, FILE_ONE}),
                Arguments.of((Object) new String[]{FILE_ONE, FLAG_SERIAL}),
                Arguments.of((Object) new String[]{FLAG_SERIAL, FILE_ONE, STDIN}),
                Arguments.of((Object) new String[]{STDIN, FILE_ONE, FLAG_SERIAL}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FLAG_SERIAL, FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO, FLAG_SERIAL}),
                Arguments.of((Object) new String[]{STDIN, FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO, STDIN}),
                Arguments.of((Object) new String[]{FLAG_SERIAL, FILE_ONE, FILE_TWO, STDIN}),
                Arguments.of((Object) new String[]{STDIN, FILE_ONE, FILE_TWO, FLAG_SERIAL})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-P"}),
                Arguments.of((Object) new String[]{"--"})
        );
    }

    @BeforeEach
    void setUp() {
        parser = new PasteArgsParser();
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() {
        assertDoesNotThrow(() -> parser.parse("-s"));
        assertEquals(VALID_FLAGS, parser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-S", "--"})
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
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> parser.parse(args));
    }

    @Test
    void isSerial_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_SERIAL));
        assertTrue(parser.isSerial());
    }

    @Test
    void isSerial_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_SERIAL, FILE_ONE));
        assertTrue(parser.isSerial());
    }

    @Test
    void isSerial_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertFalse(parser.isSerial());
    }

    @Test
    void getNonFlagArgs_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> parser.parse());
        List<String> result = parser.getNonFlagArgs();
        assertTrue(result.isEmpty());
    }

    @Test
    void getNonFlagArgs_OneNonFlagArg_ReturnsOneNonFlagArg() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        List<String> result = parser.getNonFlagArgs();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_MultipleNonFlagArgs_ReturnsMultipleNonFlagArgs() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = parser.getNonFlagArgs();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_ValidFlagAndOneNonFlagArg_ReturnsOneNonFlagArg() {
        assertDoesNotThrow(() -> parser.parse(FLAG_SERIAL, FILE_ONE));
        List<String> result = parser.getNonFlagArgs();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }
}
