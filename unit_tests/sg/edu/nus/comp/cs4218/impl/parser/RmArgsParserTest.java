package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class RmArgsParserTest {

    private static final String FLAG_RECURSIVE = "-" + RmArgsParser.FLAG_RECURSIVE;
    private static final String FLAG_EMPTY_DIR = "-" + RmArgsParser.FLAG_EMPTY_DIR;
    private static final String FILE_ONE = "file1";
    private static final String FILE_TWO = "file2";
    private static final String FILE_THREE = "file3";

    private RmArgsParser parser;

    private static Stream<Arguments> validFlags() {
        return Stream.of(
                Arguments.of(FLAG_RECURSIVE, new boolean[]{true, false}),
                Arguments.of(FLAG_EMPTY_DIR, new boolean[]{false, true}),
                Arguments.of("-rd", new boolean[]{true, true})
        );
    }

    @BeforeEach
    void setUp() {
        parser = new RmArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertTrue(parser.flags.isEmpty());
        assertTrue(parser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @MethodSource("validFlags")
    void parse_ValidFlags_CorrectMatchingFlags(String args, boolean... expectedResults) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertArrayEquals(expectedResults, new boolean[]{
                parser.isRecursive(),
                parser.isEmptyDirectory()
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"-R", "-D", "-X"})
    void parse_InvalidFlags_ThrowsInvalidArgsException(String args) {
        InvalidArgsException thrown = assertThrowsExactly(InvalidArgsException.class, () -> parser.parse(args));
        String illegalFlag = args.substring(1);
        assertEquals(String.format("illegal option -- %s", illegalFlag), thrown.getMessage());
    }

    @Test
    void getFiles_NoArg_ReturnsEmpty() {
        assertDoesNotThrow(() -> parser.parse());
        List<String> result = parser.getFiles();
        assertTrue(result.isEmpty());
    }

    @Test
    void getFiles_OneNonFlagArg_ReturnsOneFile() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE));
        assertEquals(List.of(FILE_ONE), parser.getFiles());
    }

    @Test
    void getFiles_MultipleNonFlagArg_ReturnsMultipleFiles() {
        assertDoesNotThrow(() -> parser.parse(FILE_ONE, FILE_TWO, FILE_THREE));
        List<String> result = parser.getFiles();
        List<String> expected = List.of(FILE_ONE, FILE_TWO, FILE_THREE);
        assertEquals(expected, result);
    }

    @Test
    void getFiles_ValidFlagAndOneNonFlagArg_ParsesOneFolderAndOneFlag() {
        assertDoesNotThrow(() -> parser.parse(FLAG_EMPTY_DIR, FILE_ONE));
        assertEquals(List.of(FILE_ONE), parser.getFiles());
        assertTrue(parser.isEmptyDirectory());
        assertFalse(parser.isRecursive());
    }
}
