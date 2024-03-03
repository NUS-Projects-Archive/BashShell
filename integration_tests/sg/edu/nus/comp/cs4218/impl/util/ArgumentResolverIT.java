package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class ArgumentResolverIT {

    private static final String STRING_ECHO = "echo";
    private static final Map<String, List<String>> VALID_QUOTES = new HashMap<>() {{
        put("'\"'", List.of("\""));                     // '"'
        put("'\"\"'", List.of("\"\""));                 // '""'
        put("'`'", List.of("`"));                       // '`'
        put("'```'", List.of("```"));                   // '``'
        put("'Example\"`'", List.of("Example\"`"));     // 'Example"`'
        put("\"'\"", List.of("'"));                     // "'"
        put("\"''\"", List.of("''"));                   // "''"
        put("\"Example'\"", List.of("Example'"));       // "Example'"
        // Some simplified test cases from project description
        put("'`\"\"`'", List.of("`\"\"`"));             // '`""`'
        put("'\"`\"\"`\"'", List.of("\"`\"\"`\""));     // '"`""`"'
    }};
    private ArgumentResolver argumentResolver;

    @BeforeEach
    void setUp() {
        argumentResolver = new ArgumentResolver();
    }

    /**
     * Quoting unit test case for parsing arguments when all provided arguments are valid.
     */
    @Test
    void parseArguments_ValidQuotingArgsList_ReturnsEntireParsedArgsList() {
        List<String> argsList = new ArrayList<>(VALID_QUOTES.keySet());
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argsList));
        assertEquals(VALID_QUOTES.values().stream().flatMap(List::stream).collect(Collectors.toList()), result);
    }

    /**
     * Quoting unit test case for parsing arguments if any of the provided arguments are invalid.
     */
    @Test
    void parseArguments_InvalidQuotingArgsList_ThrowsShellException() {
        List<String> argsList = new ArrayList<>(VALID_QUOTES.keySet());
        argsList.add("\"`\"");
        Throwable result = assertThrows(ShellException.class, () -> argumentResolver.parseArguments(argsList));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }

    /**
     * Globbing unit test case for parsing globbing arguments when the paths exist.
     *
     * @param tempDir Temporary directory's path
     */
    @Test
    void parseArguments_GlobbingArgsListExists_ReturnsAllPathsFound(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();

        // Create directory structure and files
        Path parent = tempDir.resolve("parent");
        assertDoesNotThrow(() -> Files.createDirectories(parent));

        Path childOne = parent.resolve("child1");
        assertDoesNotThrow(() -> Files.createDirectories(childOne));

        Path childTwo = parent.resolve("child2");
        assertDoesNotThrow(() -> Files.createDirectories(childTwo));

        assertDoesNotThrow(() -> childOne.resolve("file1.txt").toFile().createNewFile());
        assertDoesNotThrow(() -> childOne.resolve("file2.txt").toFile().createNewFile());
        assertDoesNotThrow(() -> childTwo.resolve("file3.txt").toFile().createNewFile());
        assertDoesNotThrow(() -> childTwo.resolve("file4.txt").toFile().createNewFile());

        List<String> argList = List.of("parent/child1/*", "parent/child2/*");
        List<String> expected = List.of("parent/child1/file1.txt", "parent/child1/file2.txt",
                "parent/child2/file3.txt", "parent/child2/file4.txt");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        assertEquals(expected, result);
    }

    /**
     * Globbing unit test case for parsing globbing arguments when the paths do not exist.
     *
     * @param args    The globbing arguments to be parsed
     * @param tempDir Temporary directory's path
     */
    @ParameterizedTest
    @ValueSource(strings = {"parent/*", "parent/child/*", "parent/child/grandchild/*"})
    void parseArguments_GlobbingArgsListDoNotExists_ReturnsGivenArgsList(String args, @TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();
        List<String> argList = List.of(args);
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        assertEquals(argList, result);
    }
}
