package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;

public class SequenceCommandTest {

    private Command commandMock;

    @BeforeEach
    void setUp() {
        commandMock = mock(Command.class);
    }

    @Test
    void evaluate_NonExitExceptions_AppendsErrorMessagesToOutput() {
    }

    @Test
    void  evaluate_ExitException_ContinuesAndThrowsExitExceptionAfter() {
    }

    @Test
    void evaluate_NoExceptions_ExecuteCommandsInSequence() {

    }
}
