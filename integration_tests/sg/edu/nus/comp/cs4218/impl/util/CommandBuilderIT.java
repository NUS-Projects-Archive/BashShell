package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.CommandBuilder.parseCommand;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertSameType;
import static sg.edu.nus.comp.cs4218.testutils.CustomAssertUtils.assertCallCommandListEquals;
import static sg.edu.nus.comp.cs4218.testutils.CustomAssertUtils.assertCommandListEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CommandBuilderIT {

    @ParameterizedTest
    @ValueSource(strings = {"AAA BBB CCC", "echo hello world", "ls -a"})
    void parseCommand_CallCommand_ReturnsCorrectCallCommand(String args) {
        Command cmd = assertDoesNotThrow(() -> parseCommand(args, null));
        List<String> expected = List.of(args.split(" "));
        assertSameType(CallCommand.class, cmd);
        assertEquals(expected, ((CallCommand) cmd).getArgsList());
    }

    @Test
    void parseCommand_PipeCommand_ReturnsCorrectPipeCommand() {
        String args = "paste ghost.txt | grep Line";
        Command cmd = assertDoesNotThrow(() -> parseCommand(args, null));
        List<CallCommand> expected = List.of(
                new CallCommand(List.of("paste", "ghost.txt"), null, new ArgumentResolver()),
                new CallCommand(List.of("grep", "Line"), null, new ArgumentResolver())
        );
        assertSameType(PipeCommand.class, cmd);
        assertCallCommandListEquals(expected, ((PipeCommand) cmd).getCallCommands());
    }

    @Test
    void parseCommand_SequenceCommandOfCallCall_ReturnsCorrectSequenceCommand() {
        String args = "paste ghost.txt; ls;";
        Command cmd = assertDoesNotThrow(() -> parseCommand(args, null));
        List<Command> expected = List.of(
                new CallCommand(List.of("paste", "ghost.txt"), null, new ArgumentResolver()),
                new CallCommand(List.of("ls"), null, new ArgumentResolver())
        );
        assertSameType(SequenceCommand.class, cmd);
        assertCommandListEquals(expected, ((SequenceCommand) cmd).getCommands());
    }

    @Test
    void parseCommand_SequenceCommandOfPipeCall_ReturnsCorrectSequenceCommand() {
        String args = "paste ghost.txt | grep Line; ls";
        Command cmd = assertDoesNotThrow(() -> parseCommand(args, null));
        List<Command> expected = List.of(
                new PipeCommand(List.of(
                        new CallCommand(List.of("paste", "ghost.txt"), null, new ArgumentResolver()),
                        new CallCommand(List.of("grep", "Line"), null, new ArgumentResolver())
                )),
                new CallCommand(List.of("ls"), null, new ArgumentResolver())
        );
        assertSameType(SequenceCommand.class, cmd);
        assertCommandListEquals(expected, ((SequenceCommand) cmd).getCommands());
    }
}
