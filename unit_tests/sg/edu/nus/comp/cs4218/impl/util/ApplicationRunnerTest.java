package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.ShellException;

class ApplicationRunnerTest {

    private ApplicationRunner appRunner;

    @BeforeEach
    void setUp() {
        appRunner = new ApplicationRunner();
    }

    @ParameterizedTest
    @ValueSource(strings = {"doNotExistApp", "invalidApp", "incorrectApp"})
    void runApp_InvalidApp_ThrowsShellException(String app) {
        String expectedMsg = String.format("shell: %s: Invalid app", app);
        ShellException exception = assertThrowsExactly(ShellException.class, () -> {
            String[] args = {"arg1"};
            InputStream stdin = System.in;
            OutputStream stdout = System.out;
            appRunner.runApp(app, args, stdin, stdout);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }
}
