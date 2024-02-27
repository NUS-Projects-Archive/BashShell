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
        ExitApplication exitApp = new ExitApplication(); // Given
        RuntimeException exception = assertThrows(RuntimeException.class, () -> { // When
            exitApp.run(null, null, null);
        });
        assertEquals("0", exception.getMessage()); // Then
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
        public void checkPermission(Permission perm) {
            // Empty body; Override to stop exception being thrown upon System.exit()
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new RuntimeException(String.valueOf(status));
        }
    }
}
