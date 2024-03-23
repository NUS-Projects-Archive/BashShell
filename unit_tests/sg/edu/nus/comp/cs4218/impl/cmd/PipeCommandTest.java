package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

public class PipeCommandTest {

    @Test
    void evaluate_InvalidCommand_ThrowsShellException() {
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(
                new CallCommandStub("lsa"),
                new CallCommandStub("echo", "hello", "world")
        ));

        ShellException exception = assertThrowsExactly(ShellException.class, () ->
                pipeCommand.evaluate(null, null)
        );
        String expected = "shell: lsa: Invalid app";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    void evaluate_CommandEncounterError_ThrowsAbstractApplicationException() {
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(
                new CallCommandStub("cat", "nonExistFile"),
                new CallCommandStub("echo", "hello", "world")
        ));

        AbstractApplicationException exception = assertThrowsExactly(CatException.class, () ->
                pipeCommand.evaluate(null, null)
        );
        String expected = "cat: 'nonExistFile.txt': No such file or directory";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    void evaluate_ValidCommands_ReturnsCorrectResult() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(
                new CallCommandStub("paste", "ghost.txt"),
                new CallCommandStub("grep", "Line#")
        ));

        assertDoesNotThrow(() -> pipeCommand.evaluate(null, outputStream));

        String expected = "Line# 1" + STRING_NEWLINE + "Line# 2" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void evaluate_ChainValidCommands_ReturnsCorrectResult() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(
                new CallCommandStub("paste", "ghost.txt"),
                new CallCommandStub("grep", "Line#"),
                new CallCommandStub("grep", "2")
        ));

        assertDoesNotThrow(() -> pipeCommand.evaluate(null, outputStream));

        String expected = "Line# 2" + STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }
}
