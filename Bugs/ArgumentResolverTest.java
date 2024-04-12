import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

@SuppressWarnings("PMD.NoPackage")
class ArgumentResolverTest {

    private static final String STRING_HELLO = "hello";

    private ArgumentResolver argumentResolver;

    @BeforeEach
    void setUp() {
        argumentResolver = new ArgumentResolver();
    }

    /**
     * Command Substitution unit test case to remove trailing new lines from subcommand output.
     */

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
