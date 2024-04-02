package sg.edu.nus.comp.cs4218.testutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

public final class CustomAssertUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private CustomAssertUtils() { /* Does nothing */ }

    /**
     * Assert that two {@code List<Commands>} are the same.
     * Comparison of {@code Command} depends on which type of {@code Command} it is.
     *
     * @param list1 First {@code List} of {@code Command}s
     * @param list2 Second {@code List} of {@code Command}s
     */
    public static void assertCommandListEquals(List<Command> list1, List<Command> list2) {
        assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            Command cmd1 = list1.get(i);
            Command cmd2 = list2.get(i);

            assertEquals(cmd1.getClass(), cmd2.getClass());

            if (cmd1.getClass() == CallCommand.class) {
                assertEquals(
                        ((CallCommand) cmd1).getArgsList(),
                        ((CallCommand) cmd2).getArgsList());
            } else if (cmd1.getClass() == PipeCommand.class) {
                assertCallCommandListEquals(
                        ((PipeCommand) cmd1).getCallCommands(),
                        ((PipeCommand) cmd2).getCallCommands());
            } else if (cmd1.getClass() == SequenceCommand.class) {
                assertCommandListEquals(
                        ((SequenceCommand) cmd1).getCommands(),
                        ((SequenceCommand) cmd2).getCommands());
            } else {
                fail("Unknown comparison for " + cmd1.getClass());
            }
        }
    }

    /**
     * Assert that two {@code List<CallCommands>} are the same
     *
     * @param list1 First {@code List} of {@code CallCommand} to compare
     * @param list2 Second {@code List} of {@code CallCommand} to compare
     */
    public static void assertCallCommandListEquals(List<CallCommand> list1, List<CallCommand> list2) {
        assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            assertCallCommandEquals(list1.get(i), list2.get(i));
        }
    }

    /**
     * Assert that arguments of two {@code CallCommand}s are the same
     *
     * @param cmd1 First {@code CallCommand} to compare
     * @param cmd2 Second {@code CallCommand} to compare
     */
    public static void assertCallCommandEquals(CallCommand cmd1, CallCommand cmd2) {
        assertEquals(cmd1.getArgsList(), cmd2.getArgsList());
    }
}
