package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

public class PipeCommandTest {

    ApplicationRunner mockAppRunner;
    ArgumentResolver mockArgResolver;

    @BeforeEach
    void setUp() {
        mockAppRunner = mock(ApplicationRunner.class);
        mockArgResolver = mock(ArgumentResolver.class);
    }

    @Test
    void evaluate_InvalidFirstCommand_PrintErrorMessage() {
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(
                new CallCommandStub("lsa"),
                new CallCommandStub("echo", "hello", "world")
        ));

        ShellException exception = assertThrowsExactly(ShellException.class, () ->
                pipeCommand.evaluate(null, null)
        );

        assertEquals("shell: lsa: Invalid app", exception.getMessage());
    }

    @Test
    void evaluate_validCommands_RunSuccessfully() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(
                new CallCommandStub("paste", "ghost.txt"),
                new CallCommandStub("grep", "Line#"),
                new CallCommandStub("grep", "2")
        ));

        assertDoesNotThrow(() -> pipeCommand.evaluate(null, outputStream));

        assertEquals("Line# 2" + STRING_NEWLINE, outputStream.toString());
    }
}
