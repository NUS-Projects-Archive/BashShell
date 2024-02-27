package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

public class IORedirectionHandlerTest {
    InputStream inputStream = mock(InputStream.class);
    OutputStream outputStream = mock(OutputStream.class);
    private ArgumentResolver argumentResolverMock;

    @BeforeEach
    void setUp() {
        argumentResolverMock = mock(ArgumentResolver.class);
//        inputStream = new ByteArrayInputStream("origInputStream".getBytes(StandardCharsets.UTF_8));
//        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
//        this.inputStream.close();
//        this.outputStream.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"< < input.txt", "> > output.txt", "input.txt < > output.txt", "output.txt > < input.txt"})
    void extractRedirOptions_InvalidSyntax_ThrowsShellException(String args) {
        List<String> invalidSyntax = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(invalidSyntax, inputStream, outputStream, argumentResolverMock);
        ShellException shellException = assertThrows(ShellException.class, ioRedirectionHandler::extractRedirOptions);
        assertEquals("shell: " + ERR_SYNTAX, shellException.getMessage());
    }

    @Test
    void extractRedirOptions_AmbiguousRedirect_ThrowsShellException() throws AbstractApplicationException, ShellException, IOException {
        List<String> ambiguousRedirect = Arrays.asList(">", "*.txt");
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(ambiguousRedirect, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument("*.txt"))
                .thenReturn(Arrays.asList("file1.txt", "file2.txt"));
        ShellException shellException = assertThrows(ShellException.class,
                ioRedirectionHandler::extractRedirOptions);
        assertEquals("shell: " + ERR_SYNTAX, shellException.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"> file.txt", "< file.txt > file.txt", "< file.txt"})
    void extractRedirOptions_IORedirectionOnly_GetNoRedirArgsListIsEmpty(String args) throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> IORedirectsOnly = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(IORedirectsOnly, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument("file.txt"))
                .thenReturn(Arrays.asList("file.txt"));
        ioRedirectionHandler.extractRedirOptions();
        assertTrue(ioRedirectionHandler.getNoRedirArgsList().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1.txt", "file2.txt file3.txt", "-"})
    void extractRedirOptions_NoRedirection_GetNoRedirArgsListIsNotEmpty(String args) throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> NoRedirection = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(NoRedirection, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument(anyString()))
                .thenReturn(Arrays.asList("file1.txt"), Arrays.asList("file2.txt"), Arrays.asList("file3.txt"), Arrays.asList("-"));
        ioRedirectionHandler.extractRedirOptions();
        assertFalse(ioRedirectionHandler.getNoRedirArgsList().isEmpty());
        assertSame(inputStream, ioRedirectionHandler.getInputStream());
        assertSame(outputStream, ioRedirectionHandler.getOutputStream());
    }

}
