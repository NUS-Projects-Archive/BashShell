package sg.edu.nus.comp.cs4218.impl.app;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExitApplicationTest {

    private ExitApplication app;

    @BeforeEach
    public void setUp() {
        app = new ExitApplication();
    }

    @Test
    void terminateExecution_NoArgs_ExitWithCodeZero() {
        int exitCode = assertDoesNotThrow(() ->
                catchSystemExit(() -> app.terminateExecution())
        );
        assertEquals(0, exitCode);
    }
}
