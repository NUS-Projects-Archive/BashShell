package sg.edu.nus.comp.cs4218.impl.app;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.ClassNamingConventions")
public class ExitApplicationIT {

    @Test
    void run_NoArgs_ExitCodeZero() {
        ExitApplication exitApp = new ExitApplication(); // Given
        int exitCode = assertDoesNotThrow(() ->
                catchSystemExit(() -> exitApp.run(null, null, null)) // When
        );
        assertEquals(0, exitCode);
    }
}
