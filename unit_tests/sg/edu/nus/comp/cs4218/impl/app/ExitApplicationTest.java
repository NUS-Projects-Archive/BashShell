package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.Permission;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ExitException;

/**
 * Unit test for {@link ExitApplication}.
 */
class ExitApplicationTest {

    // Issue faced: How to unit test a method that terminates the program
    // before it can return a result for assertion
    // Reference: https://www.baeldung.com/junit-system-exit

    /**
     * Instance of {@code SecurityManager} prior each test.
     * Used to restore to original after each test.
     */
    private SecurityManager securityManager;

    @BeforeEach
    void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());
        securityManager = System.getSecurityManager();
    }

    @Test
    @SuppressWarnings("PMD.MethodNamingConventions")
    void run_noArgs_exitCodeZero() {
        try {
            new ExitApplication().run(null, null, null);
        }  catch (RuntimeException exception) { //NOPMD Catch RuntimeException throw by SecurityManager.checkExit
            assertEquals("0", exception.getMessage(), "Expected exit code 0");
        } catch (ExitException e) {
            fail("Should not throw ExitException");
        }
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
            throw new RuntimeException(String.valueOf(status)); //NOPMD Can only throw RuntimeException since inherited
        }
    }
}
