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
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CutArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('c', 'b');
    private CutArgsParser cutArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{"-c", "1"}),
                Arguments.of((Object) new String[]{"-b", "1"}),
                Arguments.of((Object) new String[]{"-c", "1", "-"}),
                Arguments.of((Object) new String[]{"-b", "1", "-"}),
                Arguments.of((Object) new String[]{"-c", "1", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "1", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "1-5", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "1-5", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "1,5", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "1,5", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "1-5,10,15", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "1-5,10,15", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "1,5,10-15", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "1,5,10-15", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "1", "-"}),
                Arguments.of((Object) new String[]{"-b", "1", "-"}),
                Arguments.of((Object) new String[]{"-c", "1", "example.txt", "-"}),
                Arguments.of((Object) new String[]{"-b", "1", "example.txt", "-"}),
                Arguments.of((Object) new String[]{"-c", "1", "-", "-"}),
                Arguments.of((Object) new String[]{"-b", "1", "-", "-"}),
                Arguments.of((Object) new String[]{"-c", "1", "example1.txt", "example2.txt"}),
                Arguments.of((Object) new String[]{"-b", "1", "example1.txt", "example2.txt"})
        );
    }

    private static Stream<Arguments> invalidSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"1", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "-b", "1", "example.txt"}),
                Arguments.of((Object) new String[]{"-b", "-c", "1", "example.txt"}),
                Arguments.of((Object) new String[]{"-C", "1", "example.txt"}),
                Arguments.of((Object) new String[]{"-B", "1", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "0", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "0-5", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "0,5", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "-1", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "-1-5", "example.txt"}),
                Arguments.of((Object) new String[]{"-c", "-1,5", "example.txt"})
        );
    }

    static Stream<Arguments> validRangeList() {
        return Stream.of(
                Arguments.of(new String[]{"-c", "1", "example.txt"}, List.of(new int[]{1, 1})),
                Arguments.of(new String[]{"-c", "1-5", "example.txt"}, List.of(new int[]{1, 5})),
                Arguments.of(new String[]{"-c", "1,5", "example.txt"}, List.of(new int[]{1, 1}, new int[]{1, 5})),
                Arguments.of(new String[]{"-c", "1-5", "example.txt"}, List.of(new int[]{1, 5})),
                Arguments.of(new String[]{"-c", "1,5,10-15", "example.txt"},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15}),
                Arguments.of(new String[]{"-c", "10-15,5,1", "example.txt"},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15}),
                Arguments.of(new String[]{"-c", "1", "-"}, List.of(new int[]{1, 1})),
                Arguments.of(new String[]{"-c", "1-5", "-"}, List.of(new int[]{1, 5})),
                Arguments.of(new String[]{"-c", "1,5", "-"}, List.of(new int[]{1, 1}, new int[]{1, 5})),
                Arguments.of(new String[]{"-c", "1,5,10-15", "-"},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15}),
                Arguments.of(new String[]{"-c", "10-15,5,1", "-"},
                        List.of(new int[]{1, 1}, new int[]{1, 5}), new int[]{10, 15})
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
        String expectedMsg = String.format("illegal option -- %s", args.charAt(1));
        InvalidArgsException exception = assertThrowsExactly(InvalidArgsException.class, () -> {
            cutArgsParser.parse(args, "1", "example.txt");
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void parse_NoFlags_ThrowsInvalidArgsException() {
        String expectedMsg = "Invalid syntax"; // one valid flag is expected
        InvalidArgsException exception = assertThrowsExactly(InvalidArgsException.class, () -> {
            cutArgsParser.parse("1", "example.txt");
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_ValidSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> cutArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        // InvalidArgsException to be thrown for scenarios where no flag is provided or more than one flag is given
        assertThrows(InvalidArgsException.class, () -> cutArgsParser.parse(args));
    }

    @Test
    void isCharPo_ValidFlagAndSyntax_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1", "example.txt");
        assertTrue(cutArgsParser.isCharPo());
    }

    @Test
    void isCharPo_DifferentValidFlagAndSyntax_ReturnsFalse() throws InvalidArgsException {
        cutArgsParser.parse("-b", "1", "example.txt");
        assertFalse(cutArgsParser.isCharPo());
    }

    @Test
    void isBytePo_ValidFlagAndSyntax_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-b", "1", "example.txt");
        assertTrue(cutArgsParser.isBytePo());
    }

    @Test
    void isBytePo_DifferentValidFlagAndSyntax_ReturnsTrue() throws InvalidArgsException {
        cutArgsParser.parse("-c", "1", "example.txt");
        assertFalse(cutArgsParser.isBytePo());
    }

    @ParameterizedTest
    @MethodSource("validRangeList")
    void getRangeList_ValidList_ReturnsSortedList(String[] args, List<Integer[]> expected)
            throws InvalidArgsException {
        cutArgsParser.parse(args);
        assertEquals(cutArgsParser.getRangeList(), expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c 1 example.txt", "-c 1 -"})
    void getFileNames_OneFileGiven_ReturnsOneFile(String args) throws InvalidArgsException {
        String[] arguments = args.split("\\s+");
        String lastFile = arguments[arguments.length - 1];
        cutArgsParser.parse(arguments);
        List<String> expected = List.of(lastFile);
        List<String> result = cutArgsParser.getFileNames();
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-c 1 example1.txt example2.txt", "-c 1 - -", "-c 1 example1.txt -"})
    void getFileNames_MultipleFilesGiven_ReturnsMultipleFiles(String args) throws InvalidArgsException {
        String[] arguments = args.split("\\s+");
        String secondLastFile = arguments[arguments.length - 2];
        String lastFile = arguments[arguments.length - 1];
        cutArgsParser.parse(arguments);
        List<String> expected = List.of(secondLastFile, lastFile);
        List<String> result = cutArgsParser.getFileNames();
        assertEquals(expected, result);
    }
}
