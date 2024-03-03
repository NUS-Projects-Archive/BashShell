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

class MvArgsParserTest {

    private final static String FLAG_OVERWRITE = "-n";
    private final static String FILE_ONE = "file1";
    private final static String FILE_TWO = "file2";
    private final static String FILE_THREE = "file3";

    private final Set<Character> VALID_FLAGS = Set.of('n');
    private MvArgsParser mvArgsParser;

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
        mvArgsParser = new MvArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args)
            throws InvalidArgsException {
        mvArgsParser.parse(args);
        assertTrue(mvArgsParser.flags.isEmpty());
        assertTrue(mvArgsParser.nonFlagArgs.contains(args));
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() {
        assertDoesNotThrow(() -> mvArgsParser.parse(FLAG_OVERWRITE));
        assertEquals(VALID_FLAGS, mvArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-P", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
            mvArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> mvArgsParser.parse(args));
    }

    @Test
    void isOverwrite_NoFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> mvArgsParser.parse());
        assertTrue(mvArgsParser.isOverwrite());
    }

    @Test
    void isOverwrite_ValidFlag_ReturnsFalse() {
        assertDoesNotThrow(() -> mvArgsParser.parse(FLAG_OVERWRITE));
        assertFalse(mvArgsParser.isOverwrite());
    }

    @Test
    void isOverwrite_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> mvArgsParser.parse(FILE_ONE));
        assertTrue(mvArgsParser.isOverwrite());
    }

    @Test
    void getSourceDirectories_NoArg_ThrowsIllegalArgsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mvArgsParser.getSourceDirectories();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1", "-n file2"})
    void getSourceDirectories_OneNonFlagArg_ReturnsEmptyList(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(splitArgs(args)));
        List<String> expected = List.of();
        List<String> result = mvArgsParser.getSourceDirectories();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2", "-n file1 file2"})
    void getSourceDirectories_TwoNonFlagArgs_ReturnsFirstDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(splitArgs(args)));
        List<String> expected = List.of(FILE_ONE);
        List<String> result = mvArgsParser.getSourceDirectories();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2 file3", "-n file1 file2 file3"})
    void getSourceDirectories_MultipleNonFlagArgs_ReturnsAllDirectoriesExceptLast(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(splitArgs(args)));
        List<String> expected = List.of(FILE_ONE, FILE_TWO);
        List<String> result = mvArgsParser.getSourceDirectories();
        assertEquals(expected, result);
    }

    @Test
    void getDestinationDirectory_NoArg_ThrowsIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            mvArgsParser.getDestinationDirectory();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1", "-n file1"})
    void getDestinationDirectory_OneNonFlagArg_ReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(splitArgs(args)));
        String result = mvArgsParser.getDestinationDirectory();
        assertEquals(FILE_ONE, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2", "-n file1 file2"})
    void getDestinationDirectory_TwoNonFlagArgs_ReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(splitArgs(args)));
        String result = mvArgsParser.getDestinationDirectory();
        assertEquals(FILE_TWO, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1 file2 file3", "-n file1 file2 file3"})
    void getDestinationDirectory_MultipleNonFlagArgs_ReturnsReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(splitArgs(args)));
        String result = mvArgsParser.getDestinationDirectory();
        assertEquals(FILE_THREE, result);
    }
}
