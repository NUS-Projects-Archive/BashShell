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
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

class ArgumentResolverTest {

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

    static Stream<Arguments> getValidQuoteContents() {
        return VALID_QUOTES.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

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

    /**
     * Quoting unit test case to verify that the quote contents are returned when they are valid.
     *
     * @param validQuoteContent The valid quote content to test
     * @param expected          The expected result
     */
    @ParameterizedTest
    @MethodSource("getValidQuoteContents")
    void resolveOneArgument_ValidQuoteContents_ReturnsContents(String validQuoteContent, List<String> expected) {
        List<String> result = assertDoesNotThrow(() -> argumentResolver.resolveOneArgument(validQuoteContent));
        assertEquals(expected, result);
    }

    /**
     * Quoting unit test case to throw ShellException when the quote contents are invalid.
     *
     * @param invalidQuotes The invalid quote content to test
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "\"`\"",        // "`"
            "\"```\"",      // "```"
    })
    void resolveOneArgument_InvalidDoubleQuoteContentsWithUnmatchedBackQuote_ThrowsShellException(String invalidQuotes) {
        Throwable result = assertThrows(ShellException.class,
                () -> argumentResolver.resolveOneArgument(invalidQuotes));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }

    /**
     * Globbing unit test case to resolve one globbing argument when the paths exist.
     *
     * @param tempDir Temporary directory's path
     */
    @Test
    void resolveOneArgument_GlobbingArgExists_ReturnsAllExistingPaths(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();

        Path subDir = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDir));
        assertDoesNotThrow(() -> subDir.resolve("file1.txt").toFile().createNewFile());
        assertDoesNotThrow(() -> subDir.resolve("file2.txt").toFile().createNewFile());
        assertDoesNotThrow(() -> subDir.resolve("file3.txt").toFile().createNewFile());

        List<String> result = assertDoesNotThrow(() -> argumentResolver.resolveOneArgument("subdirectory/*"));
        assertEquals(List.of("subdirectory/file1.txt", "subdirectory/file2.txt", "subdirectory/file3.txt"), result);
    }

    /**
     * Globbing unit test case to resolve one globbing argument when the paths do not exist.
     *
     * @param tempDir Temporary directory's path
     */
    @Test
    void resolveOneArgument_GlobbingArgDoNotExists_ReturnsGivenArg(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();
        List<String> result = assertDoesNotThrow(() -> argumentResolver.resolveOneArgument("subdirectory/*"));
        assertEquals(List.of("subdirectory/*"), result);
    }

    /**
     * Command Substitution unit test case to resolve command substitution arguments with single quote.
     */
    @Test
    void resolveArgument_CommandSubstitutionWithSingleQuote_ReturnsCorrectArgs() {
        List<String> argList = List.of("echo", "`echo 'hello world'`");
        List<String> expected = List.of("echo", "hello", "world");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        assertEquals(expected, result);
    }

    /**
     * Command Substitution unit test case to resolve command substitution arguments with mixed quotes.
     */
    @Test
    void resolveArguments_CommandSubstitutionWithMixedQuotes_ReturnsCorrectArgs() {
        List<String> argList = List.of("echo", "`echo \"'quote is not interpreted as special character'\"`");
        List<String> expected = List.of("echo", "'quote", "is", "not", "interpreted", "as", "special", "character'");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        assertEquals(expected, result);
    }

    /**
     * Command Substitution unit test case to resolve command substitution arguments with invalid args.
     */
    @ParameterizedTest
    @ValueSource(strings = {"`", "`invalid command`", "echo `"})
    void resolveArguments_CommandSubstitutionInvalidArgs_ThrowsShellException(String args) {
        assertThrows(ShellException.class, () -> argumentResolver.resolveOneArgument(args));
    }

    /**
     * Command Substitution unit test case to resolve command substitution arguments with invalid newline.
     */
    @Test
    void resolveArguments_CommandSubstitutionContainsNewline_ThrowsShellException() {
        List<String> argList = List.of("echo", "`echo hello" + System.lineSeparator() + "`");
        assertThrows(ShellException.class, () -> argumentResolver.parseArguments(argList));
    }
}
