package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

public class IORedirectionHandlerTest {
    private InputStream inputStream;
    private OutputStream outputStream;
    private ArgumentResolver argResolverMock;
    private String file;
    private String fileA;
    private String fileB;
    private String fileC;


    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        argResolverMock = mock(ArgumentResolver.class);
        inputStream = new ByteArrayInputStream("origInputStream".getBytes(StandardCharsets.UTF_8));
        outputStream = new ByteArrayOutputStream();
        testDir = Files.createTempDirectory("testDir");

        Path path = testDir.resolve("file.txt");
        Path pathA = testDir.resolve("A.txt");
        Path pathB = testDir.resolve("B.txt");
        Path pathC = testDir.resolve("C.txt");

        file = path.toString();
        fileA = pathA.toString();
        fileB = pathB.toString();
        fileC = pathC.toString();

        String contentFile = "Test";
        Files.write(path, List.of(contentFile));

        String contentFileA = "Hello";
        Files.write(pathA, List.of(contentFileA));

        String contentFileB = "Java";
        Files.write(pathB, List.of(contentFileB));

        String contentFileC = "";
        Files.write(pathC, List.of(contentFileC));
    }

    @AfterEach
    void tearDown() throws IOException {
        inputStream.close();
        outputStream.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"< < input.txt", "> > output.txt", "input.txt < > output.txt", "output.txt > < input.txt"})
    void extractRedirOptions_InvalidSyntax_ThrowsShellException(String args) {
        List<String> invalidSyntax = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(invalidSyntax, inputStream, outputStream, argResolverMock);
        ShellException shellException = assertThrows(ShellException.class, ioRedirHandler::extractRedirOptions);
        assertEquals("shell: " + ERR_SYNTAX, shellException.getMessage());
    }

    @Test
    void extractRedirOptions_AmbiguousRedirect_ThrowsShellException() {
        List<String> ambiguousRedirect = Arrays.asList(">", "*.txt");
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(ambiguousRedirect, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument("*.txt"))
                    .thenReturn(Arrays.asList("file1.txt", "file2.txt"));
            ShellException shellException = assertThrows(ShellException.class,
                    ioRedirHandler::extractRedirOptions);
            assertEquals("shell: " + ERR_SYNTAX, shellException.getMessage());
        } catch (IOException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"> file.txt", "< file.txt > file.txt", "< file.txt"})
    void extractRedirOptions_IORedirectionOnly_GetNoRedirArgsListIsEmpty(String args) {
        List<String> ioRedirectsOnly = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(ioRedirectsOnly, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument("file.txt"))
                    .thenReturn(List.of("file.txt"));
            ioRedirHandler.extractRedirOptions();
            assertTrue(ioRedirHandler.getNoRedirArgsList().isEmpty());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"file1.txt", "file2.txt file3.txt", "-"})
    void extractRedirOptions_NoRedirection_GetNoRedirArgsListIsNotEmpty(String args) {
        List<String> noRedirection = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(noRedirection, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument(anyString())).thenReturn(List.of("file1.txt"), List.of("file2.txt"), List.of("file3.txt"), List.of("-"));
            ioRedirHandler.extractRedirOptions();
            assertFalse(ioRedirHandler.getNoRedirArgsList().isEmpty());
            assertSame(inputStream, ioRedirHandler.getInputStream());
            assertSame(outputStream, ioRedirHandler.getOutputStream());
        } catch (FileNotFoundException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"< A.txt", "B.txt < A.txt"})
    void extractRedirOptions_OneInputRedirection_InputStreamChangesFromOriginal(String args) {
        List<String> inputs = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument(anyString())).thenReturn(List.of(fileA), List.of(fileB), List.of(fileA));
            ioRedirHandler.extractRedirOptions();
            String expected = new String(inputStream.readAllBytes());
            String result = new String(ioRedirHandler.getInputStream().readAllBytes());
            assertNotEquals(expected, result);
            assertNotSame(inputStream, ioRedirHandler.getInputStream());
        } catch (IOException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void extractRedirOptions_MultipleInputRedirections_TakeLatestInputStream() {
        List<String> inputs = List.of("<", "A.txt", "<", "B.txt");
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument(anyString())).thenReturn(List.of(fileA), List.of(fileB));
            ioRedirHandler.extractRedirOptions();
            String expected = "Java" + STRING_NEWLINE;
            String result = new String(ioRedirHandler.getInputStream().readAllBytes());
            assertEquals(expected, result);
        } catch (IOException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"> A.txt", "B.txt > A.txt"})
    void extractRedirOptions_OneOutputRedirection_OutputStreamChangesFromOriginal(String args) {
        List<String> inputs = Arrays.asList(args.split("\\s+"));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument(anyString())).thenReturn(List.of(fileA), List.of(fileB), List.of(fileA));
            ioRedirHandler.extractRedirOptions();
            assertNotSame(outputStream, ioRedirHandler.getOutputStream());
        } catch (IOException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void extractRedirOptions_MultipleOutputRedirections_TakeLatestOutputStream() {
        List<String> inputs = List.of("<", "C.txt", ">", "B.txt", ">", "C.txt");
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(inputs, inputStream, outputStream, argResolverMock);
        try {
            when(argResolverMock.resolveOneArgument(anyString())).thenReturn(List.of(fileC), List.of(fileB), List.of(fileC));
            ioRedirHandler.extractRedirOptions();
            assertEquals(0, new String(ioRedirHandler.getInputStream().readAllBytes()).length());
            String str = "Not Empty";
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            ioRedirHandler.getOutputStream().write(bytes);
            assertTrue(new String(ioRedirHandler.getInputStream().readAllBytes()).length() > 0);
        } catch (IOException | AbstractApplicationException | ShellException e) {
            fail(e.getMessage());
        }
    }
}
