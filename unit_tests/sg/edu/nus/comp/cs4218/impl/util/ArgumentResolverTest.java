package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

// TODO: Globing and command substitution tests are not implemented yet
class ArgumentResolverTest {
    private ArgumentResolver argumentResolver;

    private static final Map<String, List<String>> VALID_QUOTE_CONTENTS = new HashMap<>() {{
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

    static Stream<Arguments> getValidQuoteContents() {
        return VALID_QUOTE_CONTENTS.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @BeforeEach
    void setUp() {
        argumentResolver = new ArgumentResolver();
    }

    /**
     * Quoting unit test case to test parseArguments returns the entire parsed args list if all the args passed are valid.
     */
    @Test
    void parseArguments_ValidArgsList_ReturnsEntireParsedArgsList() {
        List<String> argsList = new ArrayList<>(VALID_QUOTE_CONTENTS.keySet());
        List<String> result = assertDoesNotThrow(() -> argumentResolver.parseArguments(argsList));
        assertEquals(VALID_QUOTE_CONTENTS.values().stream().flatMap(List::stream).collect(Collectors.toList()),
                result);
    }

    /**
     * Quoting unit test case to test parseArguments throws ShellException if any of the args passed are invalid.
     */
    @Test
    void parseArguments_InvalidArgsList_ThrowsShellException() {
        List<String> argsList = new ArrayList<>(VALID_QUOTE_CONTENTS.keySet());
        argsList.add("\"`\"");
        Throwable result = assertThrows(ShellException.class, () -> argumentResolver.parseArguments(argsList));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }

    /**
     * Quoting unit test case to test resolveOneArgument returns the quote contents passed into the method if it is valid.
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
     * Quoting unit test case to test resolveOneArgument throws ShellException if the quote content passed into the method is invalid.
     * 
     * @param invalidQuoteContent   The invalid quote content to test
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "\"`\"",        // "`"
            "\"```\"",      // "```"
    })
    void resolveOneArgument_InvalidDoubleQuoteContentsWithUnmatchedBackQuote_ThrowsShellException
            (String invalidQuoteContent) {
        Throwable result = assertThrows(ShellException.class,
                () -> argumentResolver.resolveOneArgument(invalidQuoteContent));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }
}