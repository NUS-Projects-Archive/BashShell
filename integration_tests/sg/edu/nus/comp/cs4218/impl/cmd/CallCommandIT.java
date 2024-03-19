package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

@SuppressWarnings("PMD.ClassNamingConventions")
class CallCommandIT {
    
    private static final String SHELL_EXCEPTION = "shell: ";
    private ApplicationRunner appRunner;
    private ArgumentResolver argResolver;

    @BeforeEach
    void setUp() {
        appRunner = mock(ApplicationRunner.class);
        argResolver = new ArgumentResolver();
    }

    @Test
    void evaluate_EmptyArgsList_ThrowsShellException() {
        List<String> argsList = Collections.emptyList();
        CallCommand callCommand = new CallCommand(argsList, appRunner, argResolver);
        ShellException result = assertThrowsExactly(ShellException.class, () -> callCommand.evaluate(null, null));
        assertEquals(SHELL_EXCEPTION + ERR_SYNTAX, result.getMessage());
    }

    @Test
    void evaluate_NullArgsList_ThrowsShellException() {
        CallCommand callCommand = new CallCommand(null, appRunner, argResolver);
        ShellException result = assertThrowsExactly(ShellException.class, () -> callCommand.evaluate(null, null));
        assertEquals(SHELL_EXCEPTION + ERR_SYNTAX, result.getMessage());
    }

    @Test
    void evaluate_ValidNumberOfArguments_EvaluatesSuccessfully() {
        List<String> argsList = new ArrayList<>(List.of("app", "arguments"));
        CallCommand callCommand = new CallCommand(argsList, appRunner, argResolver);
        assertDoesNotThrow(() -> callCommand.evaluate(null, null));
        assertDoesNotThrow(() -> verify(appRunner).runApp("app", new String[]{"arguments"}, null, null));
    }

    @Test
    void evaluate_InvalidNumberOfArguments_ThrowsShellException() {
        List<String> argsList = new ArrayList<>();
        CallCommand callCommand = new CallCommand(argsList, appRunner, argResolver);
        ShellException result = assertThrowsExactly(ShellException.class, () -> callCommand.evaluate(null, null));
        assertEquals(SHELL_EXCEPTION + ERR_SYNTAX, result.getMessage());
    }

    @Test
    void evaluate_ValidApp_CallsCorrectAppRunner() {
        List<String> argsList = new ArrayList<>(List.of("echo", "arguments"));
        CallCommand callCommand = new CallCommand(argsList, appRunner, argResolver);
        assertDoesNotThrow(() -> callCommand.evaluate(null, null));
        assertDoesNotThrow(() -> verify(appRunner, times(1)).runApp(eq("echo"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("cd"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("wc"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("mkdir"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("sort"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("cat"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("exit"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("ls"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("paste"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("uniq"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("mv"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("cut"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("rm"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("tee"), any(), any(), any()));
        assertDoesNotThrow(() -> verify(appRunner, times(0)).runApp(eq("grep"), any(), any(), any()));
    }
}
