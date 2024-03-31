package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;

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
     * Quoting test case for parsing arguments when all provided arguments are valid.
     */
    @Test
    void parseArguments_ValidQuotingArgsList_ReturnsEntireParsedArgsList() {
        List<String> argsList = new ArrayList<>(VALID_QUOTES.keySet());
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argsList));
        assertEquals(VALID_QUOTES.values().stream().flatMap(List::stream).collect(Collectors.toList()), result);
    }

    /**
     * Quoting test case for parsing arguments if any of the provided arguments are invalid.
     */
    @Test
    void parseArguments_InvalidQuotingArgsList_ThrowsShellException() {
        List<String> argsList = new ArrayList<>(VALID_QUOTES.keySet());
        argsList.add("\"`\"");
        ShellException result = assertThrowsExactly(ShellException.class, () -> argumentResolver.parseArguments(argsList));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }

    /**
     * Globbing test case for parsing globbing arguments when the paths exist.
     *
     * @param tempDir Temporary directory path set as the current working directory for the test
     */
    @Test
    void parseArguments_GlobbingArgsListExists_ReturnsAllPathsFound(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();

        // Create directory structure and files
        Path parent = createNewDirectory(tempDir, "parent");
        Path child1 = createNewDirectory(parent, "child1");
        Path child2 = createNewDirectory(parent, "child2");
        createNewFile(child1, "file1.txt");
        createNewFile(child1, "file2.txt");
        createNewFile(child2, "file3.txt");
        createNewFile(child2, "file4.txt");

        List<String> argList = List.of("parent/child1/*", "parent/child2/*");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        List<String> expected = List.of("parent/child1/file1.txt", "parent/child1/file2.txt",
                "parent/child2/file3.txt", "parent/child2/file4.txt");
        assertEquals(expected, result);
    }

    /**
     * Globbing test case for parsing globbing arguments when the paths do not exist.
     *
     * @param args    The globbing arguments to be parsed
     * @param tempDir Temporary directory path set as the current working directory for the test
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

    /**
     * Globbing test case for parsing globbing arguments when enclosed in single-quotes.
     * <p>
     * When enclosed in single quotes, the globbing argument is treated as a literal string,
     * thus returning the exact string without performing any globbing operations.
     */
    @Test
    void parseArguments_GlobbingCommandInBetweenSingleQuotes_ReturnsGivenArgsListWithoutQuotes() {
        List<String> argList = List.of("'parent/child/*'");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        List<String> expected = List.of("parent/child/*");
        assertEquals(expected, result);
    }

    /**
     * Globbing test case for parsing globbing arguments when enclosed in double-quotes.
     * <p>
     * When enclosed in double quotes, the globbing argument is treated as a literal string,
     * thus returning the exact string without performing any globbing operations.
     */
    @Test
    void parseArguments_GlobbingCommandInBetweenDoubleQuotes_ReturnsGivenArgsListWithoutQuotes() {
        List<String> argList = List.of("\"parent/child/*\"");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        List<String> expected = List.of("parent/child/*");
        assertEquals(expected, result);
    }

    /**
     * Globbing test case for parsing globbing arguments when enclosed in back-quotes.
     *
     * @param tempDir Temporary directory path set as the current working directory for the test
     */
    @Test
    void parseArguments_GlobbingCommandInBetweenBackQuotes_ReturnsGlobbedFiles(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();

        // Create directory structure and files
        Path parent = createNewDirectory(tempDir, "parent");
        Path child1 = createNewDirectory(parent, "child1");
        createNewFile(child1, "file1.txt");
        createNewFile(child1, "file2.txt");

        List<String> argList = List.of("`ls parent/child1/*`");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        List<String> expected = List.of("file1.txt", "file2.txt");
        assertEquals(expected, result);
    }
}
