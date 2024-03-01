package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.AssertUtils.assertSameType;
import static sg.edu.nus.comp.cs4218.impl.util.CustomAssertUtils.assertCallCommandListEquals;
import static sg.edu.nus.comp.cs4218.impl.util.CustomAssertUtils.assertCommandListEquals;

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
    void parseCommand_CallCommand_ReturnCorrectCallCommand(String args) {
        List<String> expected = List.of(args.split(" "));

        Command cmd = assertDoesNotThrow(() -> CommandBuilder.parseCommand(args, null));

        assertSameType(CallCommand.class, cmd);
        assertEquals(expected, ((CallCommand) cmd).getArgsList());
    }

    @Test
    void parseCommand_PipeCommand_ReturnCorrectPipeCommand() {
        String args = "paste ghost.txt | grep Line";
        List<CallCommand> expected = List.of(
                new CallCommand(List.of("paste", "ghost.txt"), null, new ArgumentResolver()),
                new CallCommand(List.of("grep", "Line"), null, new ArgumentResolver())
        );

        Command cmd = assertDoesNotThrow(() -> CommandBuilder.parseCommand(args, null));

        assertSameType(PipeCommand.class, cmd);
        assertCallCommandListEquals(expected, ((PipeCommand) cmd).getCallCommands());
    }

    @Test
    void parseCommand_SequenceCommandOfCallCall_ReturnCorrectSequenceCommand() {
        String args = "paste ghost.txt; ls";
        List<Command> expected = List.of(
                new CallCommand(List.of("paste", "ghost.txt"), null, new ArgumentResolver()),
                new CallCommand(List.of("ls"), null, new ArgumentResolver())
        );

        Command cmd = assertDoesNotThrow(() -> CommandBuilder.parseCommand(args, null));

        assertSameType(SequenceCommand.class, cmd);
        assertCommandListEquals(expected, ((SequenceCommand) cmd).getCommands());
    }

    @Test
    void parseCommand_SequenceCommandOfPipeCall_ReturnCorrectSequenceCommand() {
        String args = "paste ghost.txt | grep Line; ls";
        List<Command> expected = List.of(
                new PipeCommand(List.of(
                        new CallCommand(List.of("paste", "ghost.txt"), null, new ArgumentResolver()),
                        new CallCommand(List.of("grep", "Line"), null, new ArgumentResolver())
                )),
                new CallCommand(List.of("ls"), null, new ArgumentResolver())
        );

        Command cmd = assertDoesNotThrow(() -> CommandBuilder.parseCommand(args, null));

        assertSameType(SequenceCommand.class, cmd);
        assertCommandListEquals(expected, ((SequenceCommand) cmd).getCommands());
    }
}
