package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String STRING_ECHO = "echo";
    private static final String STRING_HELLO = "hello";
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
        List<String> argList = List.of(STRING_ECHO, "`echo 'hello world'`");
        List<String> expected = List.of(STRING_ECHO, "hello", "world");
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argList));
        assertEquals(expected, result);
    }

    /**
     * Command Substitution unit test case to resolve command substitution arguments with mixed quotes.
     */
    @Test
    void resolveArguments_CommandSubstitutionWithMixedQuotes_ReturnsCorrectArgs() {
        List<String> argList = List.of(STRING_ECHO, "`echo \"'quote is not interpreted as special character'\"`");
        List<String> expected = List.of(STRING_ECHO, "'quote", "is", "not", "interpreted", "as", "special", "character'");
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
        List<String> argList = List.of(STRING_ECHO, "`echo hello" + STRING_NEWLINE + "`");
        assertThrows(ShellException.class, () -> argumentResolver.parseArguments(argList));
    }

    @Test
    void removeTrailingLineSeparator_NoLineSeparatorsInInputString_NoChanges() {
        String result = argumentResolver.removeTrailingLineSeparator(STRING_HELLO);
        assertEquals(STRING_HELLO, result);
    }

    @Test
    void removeTrailingLineSeparator_OneTrailingNewLineInInputString_TrailingNewLineRemoved() {
        String result = argumentResolver.removeTrailingLineSeparator(STRING_HELLO + STRING_NEWLINE);
        assertEquals(STRING_HELLO, result);
    }

    @Test
    void removeTrailingLineSeparator_MultipleTrailingNewLinesInInputString_TrailingNewLinesRemoved() {
        String result = argumentResolver.removeTrailingLineSeparator(STRING_HELLO + STRING_NEWLINE + STRING_NEWLINE);
        assertEquals(STRING_HELLO, result);
    }

    @Test
    void removeTrailingLineSeparator_NewLinesNotAtTheEnd_NoChanges() {
        String result = argumentResolver.removeTrailingLineSeparator(STRING_HELLO + STRING_NEWLINE + STRING_HELLO);
        assertEquals(STRING_HELLO + STRING_NEWLINE + STRING_HELLO, result);
    }
}
