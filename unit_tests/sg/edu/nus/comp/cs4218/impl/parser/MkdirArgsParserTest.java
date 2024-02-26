package sg.edu.nus.comp.cs4218.impl.parser;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class MkdirArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('p');
    private MkdirArgsParser mkdirArgsParser;

    private static Stream<Arguments> validSyntax() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"-p"}),
                Arguments.of((Object) new String[]{"example"}),
                Arguments.of((Object) new String[]{"-p", "example"}),
                Arguments.of((Object) new String[]{"example", "-p"})
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
        this.mkdirArgsParser = new MkdirArgsParser();
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() throws InvalidArgsException {
        mkdirArgsParser.parse("-p");
        assertEquals(VALID_FLAGS, mkdirArgsParser.flags, "Flags do not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "-1", "-!", "-P", "--"})
    void parse_InvalidFlag_ThrowsInvalidArgsException(String args) {
        Throwable result = assertThrows(InvalidArgsException.class, () -> {
            mkdirArgsParser.parse(args);
        });
        assertEquals(ILLEGAL_FLAG_MSG + args.charAt(1), result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validSyntax")
    void parse_validSyntax_DoNotThrowException(String... args) {
        assertDoesNotThrow(() -> mkdirArgsParser.parse(args));
    }

    @ParameterizedTest
    @MethodSource("invalidSyntax")
    void parse_invalidSyntax_ThrowsInvalidArgsException(String... args) {
        assertThrows(InvalidArgsException.class, () -> mkdirArgsParser.parse(args));
    }

    @Test
    void isCreateParent_NoFlag_ReturnsFalse() throws InvalidArgsException {
        mkdirArgsParser.parse();
        assertFalse(mkdirArgsParser.isCreateParent());
    }

    @Test
    void isCreateParent_ValidFlag_ReturnsTrue() throws InvalidArgsException {
        mkdirArgsParser.parse("-p");
        assertTrue(mkdirArgsParser.isCreateParent());
    }

    @Test
    void isCreateParent_ValidFlagAndNonFlagArg_ReturnsTrue() throws InvalidArgsException {
        mkdirArgsParser.parse("-p", "example");
        assertTrue(mkdirArgsParser.isCreateParent());
    }

    @Test
    void isCreateParent_OnlyNonFlagArg_ReturnsFalse() throws InvalidArgsException {
        mkdirArgsParser.parse("example");
        assertFalse(mkdirArgsParser.isCreateParent());
    }

    @Test
    void getDirectories_NoArg_ReturnsEmpty() throws InvalidArgsException {
        mkdirArgsParser.parse();
        List<String> result = mkdirArgsParser.getDirectories();
        assertTrue(result.isEmpty());
    }

    @Test
    void getDirectories_OneNonFlagArg_ReturnsOneFolder() throws InvalidArgsException {
        mkdirArgsParser.parse("example");
        List<String> expected = List.of("example");
        List<String> result = mkdirArgsParser.getDirectories();
        assertEquals(expected, result);
    }

    @Test
    void getDirectories_MultipleNonFlagArg_ReturnsMultipleFolder() throws InvalidArgsException {
        mkdirArgsParser.parse("example1", "example2", "example3");
        List<String> expected = List.of("example1", "example2", "example3");
        List<String> result = mkdirArgsParser.getDirectories();
        assertEquals(expected, result);
    }

    @Test
    void getDirectories_ValidFlagAndOneNonFlagArg_ReturnsOneFolder() throws InvalidArgsException {
        mkdirArgsParser.parse("-p", "example");
        List<String> expected = List.of("example");
        List<String> result = mkdirArgsParser.getDirectories();
        assertEquals(expected, result);
    }
}