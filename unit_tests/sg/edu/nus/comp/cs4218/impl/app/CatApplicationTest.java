package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.exception.CatException;

class CatApplicationTest {

    private static final String CAT_EXCEPTION_MSG = "cat: ";
    private static final String NON_EXISTENT_FILE = "nonexistent.txt";
    private static final String HELLO_WORLD = "Hello" + STRING_NEWLINE + "World";
    private static final String L1_HELLO_L2_WORLD = "1 Hello" + STRING_NEWLINE + "2 World";
    private static final String HEY_JUNIT = "Hey" + STRING_NEWLINE + "Junit";
    private static final String L1_HEY_L2_JUNIT = "1 Hey" + STRING_NEWLINE + "2 Junit";
    private static final String FROM_STDIN = "From" + STRING_NEWLINE + "Stdin";
    private static final String L1_FROM_L2_STDIN = "1 From" + STRING_NEWLINE + "2 Stdin";
    private static final String[] PARAM_TEST_VALUES = {"", "hello", HELLO_WORLD};
    private CatApplication app;
    private BufferedReader brMock;
    private InputStream inputStreamMock;
    private String fileA;
    private String fileB;

    private static List<String> getParams() {
        return Arrays.asList(PARAM_TEST_VALUES);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new CatApplication();
        brMock = mock(BufferedReader.class);
        inputStreamMock = mock(InputStream.class);

        Path testDir = Files.createTempDirectory("testDir");
        Path pathA = testDir.resolve("A.txt");
        Path pathB = testDir.resolve("B.txt");

        fileA = pathA.toString();
        fileB = pathB.toString();

        Files.write(pathA, List.of(HELLO_WORLD));
        Files.write(pathB, List.of(HEY_JUNIT));
    }

    @Test
    void readFile_FileNotFound_ThrowsCatException() {
        CatException result = assertThrowsExactly(CatException.class, () -> {
            app.readFile(false, new File(NON_EXISTENT_FILE));
        });
        assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_FILE_NOT_FOUND));
    }

    @Test
    void readFile_ValidFileNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileA)));
        String expected = HELLO_WORLD + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void readFile_ValidFileHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.readFile(true, new File(fileA)));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void readStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        when(assertDoesNotThrow(() -> brMock.readLine())).thenThrow(new IOException());
        CatException result = assertThrowsExactly(CatException.class, () -> {
            app.readStdIn(false, brMock);
        });
        assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_IO_EXCEPTION));
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void readStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        brMock = new BufferedReader(new StringReader(args));
        String result = assertDoesNotThrow(() -> app.readStdIn(false, brMock));
        String expected = args + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void readStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        brMock = new BufferedReader(new StringReader(HELLO_WORLD));
        String result = assertDoesNotThrow(() -> app.readStdIn(true, brMock));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        when(assertDoesNotThrow(() -> inputStreamMock.read())).thenThrow(new IOException());
        assertThrows(CatException.class, () -> {
            app.catStdin(false, inputStreamMock);
        });
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void catStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catStdin(false, inputStreamMock));
        assertEquals(args + STRING_NEWLINE, result);
    }

    @Test
    void catStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        inputStreamMock = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catStdin(true, inputStreamMock));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE;
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
        assertEquals(HELLO_WORLD + STRING_NEWLINE, result);
    }

    @Test
    void catFiles_OneFileHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA));
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE, result);
    }

    @Test
    void catFiles_MultipleFilesNoLineNumber_ReturnsConcatenatedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA, fileB));
        assertEquals(HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE, result);
    }

    @Test
    void catFiles_MultipleFilesHasLineNumber_ReturnsLineNumberedConcatenatedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA, fileB));
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE, result);
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void catFileAndStdin_StdinOnlyNoLineNumber_ReturnsUserInput(String args) {
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, inputStreamMock, "-"));
        String expected = args + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_StdinOnlyHasLineNumber_ReturnsLineNumberedUserInput() {
        inputStreamMock = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, inputStreamMock, "-"));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileOnlyNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, inputStreamMock, fileA, fileB));
        assertEquals(HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE, result);
    }

    @Test
    void catFileAndStdin_FileOnlyHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, inputStreamMock, fileA, fileB));
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE, result);
    }

    @Test
    void catFileAndStdin_FileAndStdInNoLineNumber_ReturnsConcatenatedFileAndStdin() {
        inputStreamMock = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, inputStreamMock, fileA, "-", fileB));
        String expected = HELLO_WORLD + STRING_NEWLINE + FROM_STDIN + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileAndStdInHasLineNumber_ReturnsLineNumberedConcatenatedFileAndStdin() {
        inputStreamMock = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, inputStreamMock, fileA, "-", fileB));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_FROM_L2_STDIN + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE;
        assertEquals(expected, result);
    }
}
