package sg.edu.nus.comp.cs4218.impl.app;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExitApplicationTest {

    @Test
    void run_noArgs_exitCodeZero() {
        ExitApplication exitApp = new ExitApplication(); // Given
        int exitCode = assertDoesNotThrow(() ->
                catchSystemExit(() -> exitApp.run(null, null, null)) // When
        );
        assertEquals(0, exitCode);
    }
}
