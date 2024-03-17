package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.ClassNamingConventions")
public class SequenceCommandIT {
    private static final String HELLO = "hello";
    private static final String SHELL_EXCEPTION = "shell: ";
    private InputStream stdin;
    private OutputStream stdout;

    private ArgumentResolver argResolver;
    private ApplicationRunner appRunner;

    private String tempFilePath;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        Path tempFile = tempDir.resolve("temp.txt");
        tempFilePath = tempFile.toString();
        Files.write(tempFile, "This is a temp file!".getBytes());

        argResolver = new ArgumentResolver();
        appRunner = new ApplicationRunner();

        stdin = new ByteArrayInputStream("test".getBytes());
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdin.close();
        stdout.close();
    }

    @Test
    void evaluate_TypicalCommandsWithOutput_WritesToStdOutInOrder() {
        try {
            List<Command> commands = new ArrayList<>();
            List<String> commandOne = new ArrayList<>(List.of("echo", HELLO));
            List<String> commandTwo = new ArrayList<>(List.of("cat", tempFilePath));
            commands.add(new CallCommand(commandOne, appRunner, argResolver));
            commands.add(new CallCommand(commandTwo, appRunner, argResolver));

            SequenceCommand seqCmd = new SequenceCommand(commands);
            seqCmd.evaluate(stdin, stdout);

            String expected = HELLO + STRING_NEWLINE + "This is a temp file!" + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void evaluate_SomeCommandsWithoutOutput_SkipsWritingForCommandsWithoutOutputs() {
        try {
            List<Command> commands = new ArrayList<>();
            List<String> commandOne = new ArrayList<>(List.of("cd", tempDir.toString()));
            List<String> commandTwo = new ArrayList<>(List.of("echo", HELLO));
            commands.add(new CallCommand(commandOne, appRunner, argResolver));
            commands.add(new CallCommand(commandTwo, appRunner, argResolver));

            SequenceCommand seqCmd = new SequenceCommand(commands);
            seqCmd.evaluate(stdin, stdout);

            String expected = HELLO + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void evaluate_SomeCommandsWithNonExitExceptions_AppendsErrorMessagesToOutput() {
        try {
            List<Command> commands = new ArrayList<>();
            List<String> commandOne = new ArrayList<>(List.of("cat", "<", ">"));
            List<String> commandTwo = new ArrayList<>(List.of("echo", HELLO));
            commands.add(new CallCommand(commandOne, appRunner, argResolver));
            commands.add(new CallCommand(commandTwo, appRunner, argResolver));

            SequenceCommand seqCmd = new SequenceCommand(commands);
            seqCmd.evaluate(stdin, stdout);

            String expected = SHELL_EXCEPTION + ERR_SYNTAX + STRING_NEWLINE + HELLO + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }
}
