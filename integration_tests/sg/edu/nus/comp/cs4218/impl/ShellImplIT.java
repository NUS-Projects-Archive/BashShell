package sg.edu.nus.comp.cs4218.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.ShellException;

@SuppressWarnings("PMD.ClassNamingConventions")
class ShellImplIT {
    private ShellImpl shellImpl;
    private ByteArrayOutputStream outContent;

    /**
     * Returns valid quote contents to be used as test input and output.
     *
     * @return
     */
    static Stream<Arguments> getValidQuoteContents() {
        return Stream.of(
                Arguments.of("echo \"`echo testing`\"", String.format("testing%s", System.lineSeparator())),
                Arguments.of("echo `echo testing`", String.format("testing%s", System.lineSeparator()))
        );
    }

    @BeforeEach
    void setUp() {
        shellImpl = new ShellImpl();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Quoting integration tests for commandStrings with some sort of valid back quotes content and
     * expects the correct output from the command.
     *
     * @param commandString String with some sort of valid back quotes content
     */
    @ParameterizedTest
    @MethodSource("getValidQuoteContents")
    void parseAndEvaluate_ValidCommandWithinBackQuotes_PrintCorrectOutput(String commandString, String expected) {
        // When
        assertDoesNotThrow(() -> shellImpl.parseAndEvaluate(commandString, System.out));

        // Then
        assertEquals(expected, outContent.toString());
    }

    /**
     * Quoting integration tests for commandStrings with some sort of invalid back quotes content
     * and expects ShellException to be thrown
     *
     * @param commandString String with some sort of invalid back quotes content
     */
    @ParameterizedTest
    @ValueSource(strings = {"\"`echdo testing`\"", "`edcho testing`"})
    void parseAndEvaluate_InvalidCommandWithinBackQuotes_ThrowsShellException(String commandString) {
        // Given
        String invalidAppName = commandString.split(" ")[0].replace("`", "").replace("\"", "");

        // When
        ShellException result = assertThrows(ShellException.class, () -> shellImpl.parseAndEvaluate(commandString, System.out));

        // Then
        assertEquals(String.format("shell: %s: %s", invalidAppName, ERR_INVALID_APP), result.getMessage());
    }
}
