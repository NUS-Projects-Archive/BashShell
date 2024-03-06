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

class MkdirArgsParserTest {

    private static final Set<Character> VALID_FLAGS = Set.of('p');
    private static final String FLAG_CR_PARENT = "-p";
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";
    private MkdirArgsParser parser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{FLAG_CR_PARENT}),
                Arguments.of((Object) new String[]{FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_CR_PARENT, FILE_ONE}),
                Arguments.of((Object) new String[]{FILE_ONE, FLAG_CR_PARENT}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FLAG_CR_PARENT, FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO, FLAG_CR_PARENT}),
                Arguments.of((Object) new String[]{FLAG_CR_PARENT, FILE_ONE, FILE_TWO, FILE_THREE}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO, FILE_THREE, FLAG_CR_PARENT})
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
        parser = new MkdirArgsParser();
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
        assertDoesNotThrow(() -> parser.parse(FLAG_CR_PARENT));
        assertEquals(VALID_FLAGS, parser.flags, "Flags do not match");
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
    void isCreateParent_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse());
        assertFalse(parser.isCreateParent());
    }

    @Test
    void isCreateParent_ValidFlag_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CR_PARENT));
        assertTrue(parser.isCreateParent());
    }

    @Test
    void isCreateParent_ValidFlagAndNonFlagArg_ReturnsTrue() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CR_PARENT, FILE_ONE));
        assertTrue(parser.isCreateParent());
    }

    @Test
    void isCreateParent_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertFalse(parser.isCreateParent());
    }

    @Test
    void getDirectories_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> parser.parse());
        List<String> result = parser.getDirectories();
        assertTrue(result.isEmpty());
    }

    @Test
    void getDirectories_OneNonFlagArg_ReturnsOneFolder() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        List<String> result = parser.getDirectories();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @Test
    void getDirectories_MultipleNonFlagArg_ReturnsMultipleFolder() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = parser.getDirectories();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getDirectories_ValidFlagAndOneNonFlagArg_ReturnsOneFolder() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CR_PARENT, FILE_ONE));
        List<String> result = parser.getDirectories();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }
}
