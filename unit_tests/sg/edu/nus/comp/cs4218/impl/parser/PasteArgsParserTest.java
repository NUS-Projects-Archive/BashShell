package sg.edu.nus.comp.cs4218.impl.parser;

import net.bytebuddy.utility.nullability.MaybeNull;
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

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

public class PasteArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('s');
    private PasteArgsParser pasteArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-s"}),
                Arguments.of((Object) new String[]{"example"}),
                Arguments.of((Object) new String[]{"-s", "example"}),
                Arguments.of((Object) new String[]{"example", "-s"})
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
        this.pasteArgsParser = new PasteArgsParser();
    }

    @Test
    void parse_Valid_Flag_ReturnsGivenMatchingFlag() throws InvalidArgsException {
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
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> pasteArgsParser.parse(args));
    }

    @Test
    void isSerial_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        pasteArgsParser.parse("-s");
        assertTrue(pasteArgsParser.isSerial());
    }

    @Test
    void isSerial_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        pasteArgsParser.parse("-s", "example");
        assertTrue(pasteArgsParser.isSerial());
    }

    @Test
    void isSerial_OnlyNonFlagArg_ReturnsFalse() throws InvalidArgsException {
        pasteArgsParser.parse("example");
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
        pasteArgsParser.parse("example");
        List<String> result = pasteArgsParser.getNonFlagArgs();
        List<String> expected = List.of("example");
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_MultipleNonFlagArgs_ReturnsMultipleNonFlagArgs() throws InvalidArgsException {
        pasteArgsParser.parse("example1", "example2", "example3");
        List<String> expected = List.of("example1", "example2", "example3");
        List<String> result = pasteArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
    }

    @Test
    void getNonFlagArgs_ValidFlagAndOneNonFlagArg_ReturnsOneNonFlagArg() throws InvalidArgsException {
        pasteArgsParser.parse("-s", "example");
        List<String> expected = List.of("example");
        List<String> result = pasteArgsParser.getNonFlagArgs();
        assertEquals(expected, result);
        assertTrue(pasteArgsParser.isSerial());
    }

}
