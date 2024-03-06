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

class MvArgsParserTest {

    private static final String FLAG_OVERWRITE = "-n";
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";

    private final Set<Character> VALID_FLAGS = Set.of('n');
    private MvArgsParser parser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{FLAG_OVERWRITE}),
                Arguments.of((Object) new String[]{FILE_ONE}),
                Arguments.of((Object) new String[]{FLAG_OVERWRITE, FILE_ONE}),
                Arguments.of((Object) new String[]{FILE_ONE, FLAG_OVERWRITE}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FLAG_OVERWRITE, FILE_ONE, FILE_TWO}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO, FLAG_OVERWRITE}),
                Arguments.of((Object) new String[]{FLAG_OVERWRITE, FILE_ONE, FILE_TWO, FILE_THREE}),
                Arguments.of((Object) new String[]{FILE_ONE, FILE_TWO, FILE_THREE, FLAG_OVERWRITE})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-N"}),
                Arguments.of((Object) new String[]{"--"})
        );
    }

    private String[] splitArgs(String args) {
        return args.split("\\s+");
    }

    @BeforeEach
    void setUp() {
        parser = new MvArgsParser();
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
        assertDoesNotThrow(() -> parser.parse(FLAG_OVERWRITE));
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
    void isOverwrite_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse());
        assertTrue(parser.isOverwrite());
    }

    @Test
    void isOverwrite_ValidFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FLAG_OVERWRITE));
        assertFalse(parser.isOverwrite());
    }

    @Test
    void isOverwrite_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertTrue(parser.isOverwrite());
    }

    @Test
    void getSourceDirectories_NoArg_ThrowsIllegalArgsException() {
        assertThrows(IllegalArgumentException.class, () -> parser.getSourceDirectories());
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1", "-n file2"})
    void getSourceDirectories_OneNonFlagArg_ReturnsEmptyList(String args) {
        assertDoesNotThrow(() -> parser.parse(splitArgs(args)));
        List<String> result = parser.getSourceDirectories();
        List<String> expected = List.of(); // expected to be empty list
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2", "-n file1 file2"})
    void getSourceDirectories_TwoNonFlagArgs_ReturnsFirstDirectory(String args) {
        assertDoesNotThrow(() -> parser.parse(splitArgs(args)));
        List<String> result = parser.getSourceDirectories();
        List<String> expected = List.of(FILE_ONE);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2 file3", "-n file1 file2 file3"})
    void getSourceDirectories_MultipleNonFlagArgs_ReturnsAllDirectoriesExceptLast(String args) {
        assertDoesNotThrow(() -> parser.parse(splitArgs(args)));
        List<String> result = parser.getSourceDirectories();
        List<String> expected = List.of(FILE_ONE, FILE_TWO);
        assertEquals(expected, result);
    }

    @Test
    void getDestinationDirectory_NoArg_ThrowsIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parser.getDestinationDirectory();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1", "-n file1"})
    void getDestinationDirectory_OneNonFlagArg_ReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> parser.parse(splitArgs(args)));
        String result = parser.getDestinationDirectory();
        assertEquals(FILE_ONE, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2", "-n file1 file2"})
    void getDestinationDirectory_TwoNonFlagArgs_ReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> parser.parse(splitArgs(args)));
        String result = parser.getDestinationDirectory();
        assertEquals(FILE_TWO, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2 file3", "-n file1 file2 file3"})
    void getDestinationDirectory_MultipleNonFlagArgs_ReturnsReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> parser.parse(splitArgs(args)));
        String result = parser.getDestinationDirectory();
        assertEquals(FILE_THREE, result);
    }
}
