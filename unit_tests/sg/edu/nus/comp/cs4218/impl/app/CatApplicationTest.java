package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
    private String fileC;

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
        Path pathC = testDir.resolve("C.txt");

        fileA = pathA.toString();
        fileB = pathB.toString();
        fileC = pathC.toString();

        String contentFileA = "Hello" + STRING_NEWLINE + "World";
        Files.write(pathA, List.of(contentFileA));

        String contentFileB = "Software" + STRING_NEWLINE + "Testing";
        Files.write(pathB, List.of(contentFileB));

        String contentFileC = "";
        Files.write(pathC, List.of(contentFileC));
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
        try {
            String result = app.readFile(false, new File(fileA));
            String expected = "Hello" + STRING_NEWLINE + "World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readFile_ValidFileHasLineNumber_ReturnsLineNumberedFileContent() {
        try {
            String result = app.readFile(true, new File(fileA));
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        try {
            when(brMock.readLine()).thenThrow(new IOException());
            Throwable result = assertThrows(CatException.class, () -> {
                app.readStdIn(false, brMock);
            });
            assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_IO_EXCEPTION));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void readStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        try {
            brMock = new BufferedReader(new StringReader(args));
            String result = app.readStdIn(false, brMock);
            String expected = args + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        try {
            brMock = new BufferedReader(new StringReader("Hello\r\nWorld"));
            String result = app.readStdIn(true, brMock);
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        try {
            when(inputStreamMock.read()).thenThrow(new IOException());
            Throwable result = assertThrows(CatException.class, () -> {
                app.catStdin(false, inputStreamMock);
            });
            assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_IO_EXCEPTION));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void catStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        try {
            inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
            String result = app.catStdin(false, inputStreamMock);
            assertEquals(args + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        try {
            inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
            String result = app.catStdin(true, inputStreamMock);
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_NoFiles_ReturnsEmptyString() {
        try {
            String result = app.catFiles(false);
            assertEquals("", result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_OneFileNoLineNumber_ReturnsFileContent() {
        try {
            String result = app.catFiles(false, fileA);
            assertEquals("Hello\r\nWorld" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_OneFileHasLineNumber_ReturnsLineNumberedFileContent() {
        try {
            String result = app.catFiles(true, fileA);
            assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_MultipleFilesNoLineNumber_ReturnsConcatenatedFileContent() {
        try {
            String result = app.catFiles(false, fileA, fileB);
            assertEquals("Hello\r\nWorld\r\nSoftware\r\nTesting" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_MultipleFilesHasLineNumber_ReturnsLineNumberedConcatenatedFileContent() {
        try {
            String result = app.catFiles(true, fileA, fileB);
            assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n2 Testing" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void catFileAndStdin_StdinOnlyNoLineNumber_ReturnsUserInput(String args) {
        try {
            inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(false, inputStreamMock, "-");
            assertEquals(args + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_StdinOnlyHasLineNumber_ReturnsLineNumberedUserInput() {
        try {
            inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(true, inputStreamMock, "-");
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileOnlyNoLineNumber_ReturnsFileContent() {
        try {
            String result = app.catFileAndStdin(false, inputStreamMock, fileA, fileB);
            assertEquals("Hello\r\nWorld\r\nSoftware\r\nTesting" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileOnlyHasLineNumber_ReturnsLineNumberedFileContent() {
        try {
            String result = app.catFileAndStdin(true, inputStreamMock, fileA, fileB);
            assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n2 Testing" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileAndStdInNoLineNumber_ReturnsConcatenatedFileAndStdin() {
        try {
            inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(false, inputStreamMock, fileA, "-", fileB);
            String expected = "Hello\r\nWorld\r\nFrom\r\n" + "Stdin\r\nSoftware\r\nTesting" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileAndStdInHasLineNumber_ReturnsLineNumberedConcatenatedFileAndStdin() {
        try {
            inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(true, inputStreamMock, fileA, "-", fileB);
            String expected = "1 Hello\r\n2 World\r\n1 From\r\n" + "2 Stdin\r\n1 Software\r\n2 Testing" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }
}
