package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.skeleton.parser.CutArgsParser;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

public class CutArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('c', 'b');
    private CutArgsParser cutArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-c"}),
                Arguments.of((Object) new String[]{"-b"}),
                Arguments.of((Object) new String[]{"-c", "example"}),
                Arguments.of((Object) new String[]{"-b", "example"}),
                Arguments.of((Object) new String[]{"-c", "-"}),
                Arguments.of((Object) new String[]{"-b", "-"}),
                Arguments.of((Object) new String[]{"-c", "example"}),
                Arguments.of((Object) new String[]{"-b", "example"})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-c", "-a", "example"}),
                Arguments.of((Object) new String[]{"-b", "--", "example"}),
                Arguments.of((Object) new String[]{"-C"}),
                Arguments.of((Object) new String[]{"-B"})
        );
    }

    @BeforeEach
    void setUp() {
        this.cutArgsParser = new CutArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args)
            throws InvalidArgsException {
        cutArgsParser.parse(args);
        assertTrue(cutArgsParser.flags.isEmpty());
        assertTrue(cutArgsParser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c", "-b"})
    void parse_ValidFlag_ShouldMatchGivenFlags(String args) throws InvalidArgsException {
        cutArgsParser.parse(args);

        // Retain only the common elements between cutArgsParser.flags and VALID_FLAGS
        cutArgsParser.flags.retainAll(VALID_FLAGS);

        // Check if there's at least one common element
        assertFalse(cutArgsParser.flags.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-C", "-B", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
            cutArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> cutArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
    }

    @Test
    void isCutByChar_NoFlag_ReturnsFalse() throws InvalidArgsException {
        cutArgsParser.parse();
        assertFalse(cutArgsParser.isCutByChar());
    }

    @Test
    void isCutByChar_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-c");
        assertTrue(cutArgsParser.isCutByChar());
    }

    @Test
    void isCutByChar_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-c", "example");
        assertTrue(cutArgsParser.isCutByChar());
    }

    @Test
    void isCutByChar_OnlyNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("example");
        assertFalse(cutArgsParser.isCutByChar());
    }

    @Test
    void isCutByByte_NoFlag_ReturnsFalse() throws InvalidArgsException {
        cutArgsParser.parse();
        assertFalse(cutArgsParser.isCutByByte());
    }

    @Test
    void isCutByByte_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-b");
        assertTrue(cutArgsParser.isCutByByte());
    }

    @Test
    void isCutByByte_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-b", "example");
        assertTrue(cutArgsParser.isCutByByte());
    }

    @Test
    void isCutByByte_OnlyNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("example");
        assertFalse(cutArgsParser.isCutByByte());
    }

    @Test
    void getFileNames_NoArg_ReturnsEmpty() throws InvalidArgsException {
        cutArgsParser.parse();
        List<String> result = cutArgsParser.getFileNames();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFileNames_OneNonFlagArg_ReturnsOneFile() throws InvalidArgsException {
        cutArgsParser.parse("example");
        List<String> expected = List.of("example");
        List<String> result = cutArgsParser.getFileNames();
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_MultipleNonFlagArg_ReturnsMultipleFiles() throws InvalidArgsException {
        cutArgsParser.parse("example1", "example2", "example3");
        List<String> expected = List.of("example1", "example2", "example3");
        List<String> result = cutArgsParser.getFileNames();
        assertEquals(expected, result);
    }

    @Test
    void getFileNames_ValidFlagsAndOneNonFlagArg_ReturnsOneFile() throws InvalidArgsException {
        cutArgsParser.parse("-c", "-b", "example");
        List<String> expected = List.of("example");
        List<String> result = cutArgsParser.getFileNames();
        assertEquals(expected, result);
    }
}
