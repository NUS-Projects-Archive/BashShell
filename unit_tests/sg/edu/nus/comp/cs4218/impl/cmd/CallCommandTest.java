package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallCommandTest {

    private CallCommand callCommand;
    private ArgumentResolver argResolverMock;
    private ApplicationRunner appRunnerMock;
    private IORedirectionHandler ioRedirMock;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static final String SHELL_EXCEPTION = "shell: ";

    @BeforeEach
    void setUp() throws IOException {
        argResolverMock = mock(ArgumentResolver.class);
        appRunnerMock = mock(ApplicationRunner.class);
        ioRedirMock = mock(IORedirectionHandler.class);
        inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
    }

    @Test
    void evaluate_ArgsListIsEmpty_ThrowsShellException() {
        List<String> emptyArgsList = Collections.emptyList();
        callCommand = new CallCommand(emptyArgsList, appRunnerMock, argResolverMock);
        Throwable result = assertThrows(ShellException.class, () -> {
            callCommand.evaluate(inputStream, outputStream);
        });
        assertEquals(SHELL_EXCEPTION + ERR_SYNTAX, result.getMessage());
    }

    @Test
    void evaluate_ArgsListIsNull_ThrowsShellException() {
        callCommand = new CallCommand(null, appRunnerMock, argResolverMock);
        Throwable result = assertThrows(ShellException.class, () -> {
            callCommand.evaluate(inputStream, outputStream);
        });
        assertEquals(SHELL_EXCEPTION + ERR_SYNTAX, result.getMessage());
    }

    @Test
    void evaluate_HasArgs_CallsCorrectAppRunner() throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> argsList = new ArrayList<>(List.of("echo", "hello"));
        callCommand = new CallCommand(argsList, appRunnerMock, argResolverMock);
        when(ioRedirMock.getNoRedirArgsList()).thenReturn(argsList);
        when(argResolverMock.parseArguments(argsList)).thenReturn(argsList);
        callCommand.evaluate(inputStream, outputStream);
        verify(appRunnerMock, times(1)).runApp(eq("echo"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("ls"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("grep"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("cd"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("mkdir"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("cat"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("paste"), any(), any(), any());
        verify(appRunnerMock, times(0)).runApp(eq("exit"), any(), any(), any());
    }
}
