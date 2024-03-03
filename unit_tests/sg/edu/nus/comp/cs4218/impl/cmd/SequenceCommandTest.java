package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

public class SequenceCommandTest {

    private Command validCmdHasOutput;
    private Command validCmdNoOutput;
    private Command nonExitExceptnCmd;
    private InputStream stdin;
    private OutputStream stdout;
    private static final String SHELL_EXCEPTION = "shell: ";
    private static final String HELLO = "hello";

    @BeforeEach
    public void setUp() throws Exception {
        ArgumentResolver argResolver = new ArgumentResolver();
        ApplicationRunner appRunner = new ApplicationRunner();
        stdin = new ByteArrayInputStream("test".getBytes());
        stdout = new ByteArrayOutputStream();

        List<String> validArgsHasOut = new ArrayList<>(List.of("echo", HELLO));
        List<String> validArgsNoOut = new ArrayList<>(List.of("cd", ".."));
        List<String> nonExitExceptnArg = new ArrayList<>(List.of("cat", "<", ">"));

        validCmdHasOutput = new CallCommand(validArgsHasOut, appRunner, argResolver);
        validCmdNoOutput = new CallCommand(validArgsNoOut, appRunner, argResolver);
        nonExitExceptnCmd = new CallCommand(nonExitExceptnArg, appRunner, argResolver);
    }

    @AfterEach
    void tearDown() throws IOException {
        stdin.close();
        stdout.close();
    }

    @Test
    void evaluate_NoExceptions_PrintsOutputOfCommandsInSequence() {
        try {
            List<Command> spyCommands = new ArrayList<>();
            spyCommands.add(spy(validCmdHasOutput));
            spyCommands.add(spy(validCmdHasOutput));

            SequenceCommand seqCmd = new SequenceCommand(spyCommands);
            seqCmd.evaluate(stdin, stdout);

            String expected = HELLO + STRING_NEWLINE + HELLO + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @EnabledOnOs({OS.WINDOWS, OS.MAC})
    void evaluate_NoExceptions_PrintsOnlyOutputOfCommandsWithOutputInSequence() {
        try {
            List<Command> spyCommands = new ArrayList<>();
            spyCommands.add(spy(validCmdNoOutput));
            spyCommands.add(spy(validCmdHasOutput));

            SequenceCommand seqCmd = new SequenceCommand(spyCommands);
            seqCmd.evaluate(stdin, stdout);

            String expected = HELLO + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void evaluate_NonExitExceptions_AppendsErrorMessagesToOutput() {

        try {
            List<Command> spyCommands = new ArrayList<>();
            spyCommands.add(spy(nonExitExceptnCmd));
            spyCommands.add(spy(validCmdHasOutput));

            SequenceCommand seqCmd = new SequenceCommand(spyCommands);
            seqCmd.evaluate(stdin, stdout);

            String expected = SHELL_EXCEPTION + ERR_SYNTAX + STRING_NEWLINE + HELLO + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void evaluate_ExitException_ContinuesAndThrowsExitExceptionAfter() {
        try {
            Command mockCmd = mock(Command.class);
            doThrow(new ExitException("1")).when(mockCmd).evaluate(stdin, stdout);

            List<Command> spyCommands = new ArrayList<>();
            spyCommands.add(mockCmd);
            spyCommands.add(spy(validCmdHasOutput));

            SequenceCommand seqCmd = new SequenceCommand(spyCommands);
            Throwable result = assertThrows(ExitException.class, () -> {
                seqCmd.evaluate(stdin, stdout);
                mockCmd.evaluate(stdin, stdout);
            });

            String expected = HELLO + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
            assertEquals("exit: 1", result.getMessage());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }
}
