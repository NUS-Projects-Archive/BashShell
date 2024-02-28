package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class IORedirectionHandlerTest {
    private InputStream inputStream;
    private OutputStream outputStream;
    private ArgumentResolver argumentResolverMock;

    private String fileA;
    private String fileB;
    private String fileC;


    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        argumentResolverMock = mock(ArgumentResolver.class);
        inputStream = new ByteArrayInputStream("origInputStream".getBytes(StandardCharsets.UTF_8));
        outputStream = new ByteArrayOutputStream();
        testDir = Files.createTempDirectory("testDir");

        Path pathA = testDir.resolve("A.txt");
        Path pathB = testDir.resolve("B.txt");
        Path pathC = testDir.resolve("C.txt");

        this.fileA = pathA.toString();
        this.fileB = pathB.toString();
        this.fileC = pathC.toString();

        String contentFileA = "Hello";
        Files.write(pathA, List.of(contentFileA));

        String contentFileB = "Java";
        Files.write(pathB, List.of(contentFileB));

        String contentFileC = "";
        Files.write(pathC, List.of(contentFileC));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
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
                .thenReturn(List.of("file.txt"));
        ioRedirectionHandler.extractRedirOptions();
        assertTrue(ioRedirectionHandler.getNoRedirArgsList().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1.txt", "file2.txt file3.txt", "-"})
    void extractRedirOptions_NoRedirection_GetNoRedirArgsListIsNotEmpty(String args) throws FileNotFoundException, AbstractApplicationException, ShellException {
        List<String> NoRedirection = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(NoRedirection, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument(anyString()))
                .thenReturn(List.of("file1.txt"), List.of("file2.txt"), List.of("file3.txt"), List.of("-"));
        ioRedirectionHandler.extractRedirOptions();
        assertFalse(ioRedirectionHandler.getNoRedirArgsList().isEmpty());
        assertSame(inputStream, ioRedirectionHandler.getInputStream());
        assertSame(outputStream, ioRedirectionHandler.getOutputStream());
    }

    @ParameterizedTest
    @ValueSource(strings = {"< A.txt", "B.txt < A.txt"})
    void extractRedirOptions_OneInputRedirection_InputStreamChangesFromOriginal(String args) throws IOException, AbstractApplicationException, ShellException {
        List<String> inputs = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument(anyString()))
                .thenReturn(List.of(this.fileA), List.of(this.fileB), List.of(this.fileA));
        ioRedirectionHandler.extractRedirOptions();
        String expected = new String(this.inputStream.readAllBytes());
        String result = new String(ioRedirectionHandler.getInputStream().readAllBytes());
        assertNotEquals(expected, result);
        assertNotSame(inputStream, ioRedirectionHandler.getInputStream());
    }

    @Test
    void extractRedirOptions_MultipleInputRedirections_TakeLatestInputStream() throws IOException, AbstractApplicationException, ShellException {
        List<String> inputs = List.of("<", "A.txt", "<", "B.txt");
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument(anyString()))
                .thenReturn(List.of(this.fileA), List.of(this.fileB));
        ioRedirectionHandler.extractRedirOptions();
        String expected = "Java" + STRING_NEWLINE;
        String result = new String(ioRedirectionHandler.getInputStream().readAllBytes());
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"> A.txt", "B.txt > A.txt"})
    void extractRedirOptions_OneOutputRedirection_OutputStreamChangesFromOriginal(String args) throws IOException, AbstractApplicationException, ShellException {
        List<String> inputs = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument(anyString()))
                .thenReturn(List.of(this.fileA), List.of(this.fileB), List.of(this.fileA));
        ioRedirectionHandler.extractRedirOptions();
        assertNotSame(outputStream, ioRedirectionHandler.getOutputStream());
    }

    @Test
    void extractRedirOptions_MultipleOutputRedirections_TakeLatestOutputStream() throws IOException, AbstractApplicationException, ShellException {
        List<String> inputs = List.of("<", "C.txt", ">", "B.txt", ">", "C.txt");
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argumentResolverMock);
        when(argumentResolverMock.resolveOneArgument(anyString()))
                .thenReturn(List.of(this.fileC), List.of(this.fileB), List.of(this.fileC));
        ioRedirectionHandler.extractRedirOptions();
        assertEquals(0, new String(ioRedirectionHandler.getInputStream().readAllBytes()).length());
        String str = "Not Empty";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        ioRedirectionHandler.getOutputStream().write(bytes);
        assertTrue(new String(ioRedirectionHandler.getInputStream().readAllBytes()).length() > 0);
    }
}
