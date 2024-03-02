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

    private final Set<Character> VALID_FLAGS = Set.of('n');
    private MvArgsParser mvArgsParser;

    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-n"}),
                Arguments.of((Object) new String[]{"example"}),
                Arguments.of((Object) new String[]{"-n", "example"}),
                Arguments.of((Object) new String[]{"example1, example2"}),
                Arguments.of((Object) new String[]{"-n", "example1, example2"}),
                Arguments.of((Object) new String[]{"example1", "example2", "-n"})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-N"}),
                Arguments.of((Object) new String[]{"--"})
        );
    }

    @BeforeEach
    void setUp() {
        this.mvArgsParser = new MvArgsParser();
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
        assertDoesNotThrow(() -> mvArgsParser.parse("-n"));
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
        assertDoesNotThrow(() -> mvArgsParser.parse("-n"));
        assertFalse(mvArgsParser.isOverwrite());
    }

    @Test
    void isOverwrite_OnlyNonFlagArg_ReturnsFalse() {
        assertDoesNotThrow(() -> mvArgsParser.parse("example"));
        assertTrue(mvArgsParser.isOverwrite());
    }

    @Test
    void getSourceDirectories_NoArg_ThrowsIllegalArgsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mvArgsParser.getSourceDirectories();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"example", "-n example"})
    void getSourceDirectories_OneNonFlagArg_ReturnsEmptyList(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args.split("\\s+")));
        List<String> expected = List.of();
        List<String> result = mvArgsParser.getSourceDirectories();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"example1 example2", "-n example1 example2"})
    void getSourceDirectories_TwoNonFlagArgs_ReturnsFirstDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args.split("\\s+")));
        List<String> expected = List.of("example1");
        List<String> result = mvArgsParser.getSourceDirectories();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"example1 example2 example3", "-n example1 example2 example3"})
    void getSourceDirectories_MultipleNonFlagArgs_ReturnsAllDirectoriesExceptLast(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args.split("\\s+")));
        List<String> expected = List.of("example1", "example2");
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
    @ValueSource(strings = {"example", "-n example"})
    void getDestinationDirectory_OneNonFlagArg_ReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args.split("\\s+")));
        String expected = "example";
        String result = mvArgsParser.getDestinationDirectory();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"example1 example2", "-n example1 example2"})
    void getDestinationDirectory_TwoNonFlagArgs_ReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args.split("\\s+")));
        String expected = "example2";
        String result = mvArgsParser.getDestinationDirectory();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"example1 example2 example3", "-n example1 example2 example3"})
    void getDestinationDirectory_MultipleNonFlagArgs_ReturnsReturnsLastDirectory(String args) {
        assertDoesNotThrow(() -> mvArgsParser.parse(args.split("\\s+")));
        String expected = "example3";
        String result = mvArgsParser.getDestinationDirectory();
        assertEquals(expected, result);
    }
}