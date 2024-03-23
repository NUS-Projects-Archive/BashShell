package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.ShellException;

class CommandBuilderTest {

    private static Stream<String> getNewlineInQuotes() {
        return Stream.of(
                "'" + STRING_NEWLINE + "'",
                "\"" + STRING_NEWLINE + "\"",
                "`" + STRING_NEWLINE + "`"
        );
    }

    /**
     * Quoting unit tests for commandStrings with \n and expects a Shell Exception.
     *
     * @param newlineInQuotes String with newline within quotes
     */
    @ParameterizedTest
    @MethodSource("getNewlineInQuotes")
    void parseCommand_NewlineInQuotes_ThrowsShellException(String newlineInQuotes) {
        Throwable result = assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(newlineInQuotes, null));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }

    /**
     * Quoting unit tests for commandStrings with unmatched quotes and expects a Shell Exception.
     *
     * @param unmatchedQuotes Unmatched quotes in String
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "'",            // '
            "\"",           // "
            "\"\"\"",       // """
            "`",            // `
            "```",          // ```
    })
    void parseCommand_UnmatchedQuotes_ThrowsShellException(String unmatchedQuotes) {
        Throwable result = assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(unmatchedQuotes, null));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }
}