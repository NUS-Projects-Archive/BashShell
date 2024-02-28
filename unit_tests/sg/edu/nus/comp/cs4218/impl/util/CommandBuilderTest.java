package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.ShellException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

class CommandBuilderTest {
    /**
     * Tests for commandStrings with unmatched quotes and expects a Shell Exception.
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