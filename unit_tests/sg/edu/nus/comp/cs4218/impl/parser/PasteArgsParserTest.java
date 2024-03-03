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

public class PasteArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('s');
    private static final String FLAG_SERIAL = "-s";
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";
    private static final String STDIN = "-";
    private PasteArgsParser pasteArgsParser;

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
        pasteArgsParser = new PasteArgsParser();
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() throws InvalidArgsException {
        pasteArgsParser.parse("-s");
        assertEquals(VALID_FLAGS, pasteArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-S", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
            pasteArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> pasteArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_InvalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> pasteArgsParser.parse(args));
    }

    @Test
    void isSerial_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        pasteArgsParser.parse(FLAG_SERIAL);
        assertTrue(pasteArgsParser.isSerial());
    }

    @Test
    void isSerial_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        pasteArgsParser.parse(FLAG_SERIAL, FILE_ONE);
        assertTrue(pasteArgsParser.isSerial());
    }

    @Test
    void isSerial_OnlyNonFlagArg_ReturnsFalse() throws InvalidArgsException {
        pasteArgsParser.parse(FILE_ONE);
        assertFalse(pasteArgsParser.isSerial());
    }

    @Test
    void getNonFlagArgs_NoArg_ReturnsEmpty() throws InvalidArgsException {
        pasteArgsParser.parse();
        List<String> result = pasteArgsParser.getNonFlagArgs();
        assertTrue(result.isEmpty());
    }

    @Test
    void getNonFlagArgs_OneNonFlagArg_ReturnsOneNonFlagArg() throws InvalidArgsException {
        pasteArgsParser.parse(FILE_ONE);
        List<String> result = pasteArgsParser.getNonFlagArgs();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_MultipleNonFlagArgs_ReturnsMultipleNonFlagArgs() throws InvalidArgsException {
        pasteArgsParser.parse(FILE_ONE, FILE_TWO, FILE_THREE);
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        List<String> result = pasteArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_ValidFlagAndOneNonFlagArg_ReturnsOneNonFlagArg() throws InvalidArgsException {
        pasteArgsParser.parse(FLAG_SERIAL, FILE_ONE);
        List<String> expected = List.of(FILE_ONE);
        List<String> result = pasteArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
    }
}
