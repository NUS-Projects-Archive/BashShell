package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.Permission;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExitApplicationTest {

    // Issue faced: How to unit test a method that terminates the program
    // before it can return a result for assertion
    // Reference: https://www.baeldung.com/junit-system-exit

    private SecurityManager securityManager;

    @BeforeEach
    void init() {
        System.setSecurityManager(new NoExitSecurityManager());
        securityManager = System.getSecurityManager();
    }

    @Test
    void run_noArgs_exitCodeZero() {
        // Given
        final String expected = "0"; // Expects exit code 0

        // When
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            new ExitApplication().run(null, null, null);
        });

        // Then
        assertEquals(expected, exception.getMessage(), "Expected exit code 0");
    }

    @AfterEach
    void tearDown() {
        System.setSecurityManager(securityManager);
    }

    /**
     * Custom class for {@link SecurityManager} that stops {@code SecurityManager}
     * from throwing an exception when {@code System.exit()} is called.
     * This prevents the program from terminating.
     */
    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(final Permission perm) {
            // Empty body; Override to stop exception being thrown upon System.exit()
        }

        @Override
        public void checkExit(final int status) {
            super.checkExit(status);
            throw new RuntimeException(String.valueOf(status));
        }
    }
}
