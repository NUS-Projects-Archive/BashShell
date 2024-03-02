package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.CatException;

class CatApplicationTest {

    private static final String CAT_EXCEPTION_MSG = "cat: ";
    private static final String NON_EXISTENT_FILE = "nonexistent.txt";
    private CatApplication app;
    private BufferedReader brMock;
    private InputStream inputStreamMock;
    private String fileA;
    private String fileB;

    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        app = new CatApplication();
        brMock = mock(BufferedReader.class);
        inputStreamMock = mock(InputStream.class);
        testDir = Files.createTempDirectory("testDir");

        Path pathA = testDir.resolve("A.txt");
        Path pathB = testDir.resolve("B.txt");

        fileA = pathA.toString();
        fileB = pathB.toString();

        String contentFileA = "Hello" + STRING_NEWLINE + "World";
        Files.write(pathA, List.of(contentFileA));

        String contentFileB = "Software" + STRING_NEWLINE + "Testing";
        Files.write(pathB, List.of(contentFileB));
    }

    @Test
    void readFile_FileNotFound_ThrowsCatException() {
        Throwable result = assertThrows(CatException.class, () -> {
            app.readFile(false, new File(NON_EXISTENT_FILE));
        });
        assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_FILE_NOT_FOUND));
    }

    @Test
    void readFile_ValidFileNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileA)));
        String expected = "Hello" + STRING_NEWLINE + "World" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void readFile_ValidFileHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.readFile(true, new File(fileA)));
        String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void readStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        when(assertDoesNotThrow(() -> brMock.readLine())).thenThrow(new IOException());
        Throwable result = assertThrows(CatException.class, () -> {
            app.readStdIn(false, brMock);
        });
        assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_IO_EXCEPTION));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void readStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        brMock = new BufferedReader(new StringReader(args));
        String result = assertDoesNotThrow(() -> app.readStdIn(false, brMock));
        String expected = args + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void readStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        brMock = new BufferedReader(new StringReader("Hello\r\nWorld"));
        String result = assertDoesNotThrow(() -> app.readStdIn(true, brMock));
        String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        when(assertDoesNotThrow(() -> inputStreamMock.read())).thenThrow(new IOException());
        Throwable result = assertThrows(CatException.class, () -> {
            app.catStdin(false, inputStreamMock);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void catStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catStdin(false, inputStreamMock));
        assertEquals(args + STRING_NEWLINE, result);
    }

    @Test
    void catStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catStdin(true, inputStreamMock));
        String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catFiles_NoFiles_ReturnsEmptyString() {
        String result = assertDoesNotThrow(() -> app.catFiles(false));
        assertEquals("", result);
    }

    @Test
    void catFiles_OneFileNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA));
        assertEquals("Hello\r\nWorld" + STRING_NEWLINE, result);
    }

    @Test
    void catFiles_OneFileHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA));
        assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, result);
    }

    @Test
    void catFiles_MultipleFilesNoLineNumber_ReturnsConcatenatedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA, fileB));
        assertEquals("Hello\r\nWorld\r\nSoftware\r\nTesting" + STRING_NEWLINE, result);
    }

    @Test
    void catFiles_MultipleFilesHasLineNumber_ReturnsLineNumberedConcatenatedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA, fileB));
        assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n2 Testing" + STRING_NEWLINE, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void catFileAndStdin_StdinOnlyNoLineNumber_ReturnsUserInput(String args) {
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, inputStreamMock, "-"));
        assertEquals(args + STRING_NEWLINE, result);
    }

    @Test
    void catFileAndStdin_StdinOnlyHasLineNumber_ReturnsLineNumberedUserInput() {
        inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, inputStreamMock, "-"));
        String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileOnlyNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, inputStreamMock, fileA, fileB));
        assertEquals("Hello\r\nWorld\r\nSoftware\r\nTesting" + STRING_NEWLINE, result);
    }

    @Test
    void catFileAndStdin_FileOnlyHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, inputStreamMock, fileA, fileB));
        assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n2 Testing" + STRING_NEWLINE, result);
    }

    @Test
    void catFileAndStdin_FileAndStdInNoLineNumber_ReturnsConcatenatedFileAndStdin() {
        inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, inputStreamMock, fileA, "-", fileB));
        String expected = "Hello\r\nWorld\r\nFrom\r\n" + "Stdin\r\nSoftware\r\nTesting" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileAndStdInHasLineNumber_ReturnsLineNumberedConcatenatedFileAndStdin() {
        inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, inputStreamMock, fileA, "-", fileB));
        String expected = "1 Hello\r\n2 World\r\n1 From\r\n" + "2 Stdin\r\n1 Software\r\n2 Testing" + STRING_NEWLINE;
        assertEquals(expected, result);
    }
}
