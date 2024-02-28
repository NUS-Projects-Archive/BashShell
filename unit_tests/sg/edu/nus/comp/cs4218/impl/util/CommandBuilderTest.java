package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

class CommandBuilderTest {
    private static Stream<String> getNewlineInQuotes() {
        return Stream.of(
            "'" + STRING_NEWLINE + "'",
            "\"" + STRING_NEWLINE + "\"",
            "`" + STRING_NEWLINE + "`"
        );
    }

    /**
     * Tests for commandStrings with \n and expects a Shell Exception.
     */  
    @ParameterizedTest
    @MethodSource("getNewlineInQuotes")
    void parseCommand_NewlineInQuotes_ThrowsShellException(String newlineInQuotes) {
        Throwable result = assertThrows(ShellException.class, () -> CommandBuilder.parseCommand(newlineInQuotes, null));
        assertEquals(String.format("shell: %s", ERR_SYNTAX), result.getMessage());
    }
    
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