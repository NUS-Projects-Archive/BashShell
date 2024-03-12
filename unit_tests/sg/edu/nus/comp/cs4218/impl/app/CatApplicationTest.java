package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.AssertUtils.assertEmptyString;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.exception.CatException;

class CatApplicationTest {

    private static final String HELLO_WORLD = "Hello" + STRING_NEWLINE + "World";
    private static final String L1_HELLO_L2_WORLD = "1 Hello" + STRING_NEWLINE + "2 World";
    private static final String HEY_JUNIT = "Hey" + STRING_NEWLINE + "Junit";
    private static final String L1_HEY_L2_JUNIT = "1 Hey" + STRING_NEWLINE + "2 Junit";
    private static final String[] PARAM_TEST_VALUES = {"", "hello", HELLO_WORLD};
    private static final String ERR_NON_EXIST = "cat: 'nonExistFile.txt': No such file or directory";
    private CatApplication app;
    private InputStream inputStreamMock;

    @TempDir
    private Path tempDir;
    private Path pathA;
    private String fileA;
    private String fileB;
    private String nonExistFile;

    private static List<String> getParams() {
        return Arrays.asList(PARAM_TEST_VALUES);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new CatApplication();
        inputStreamMock = mock(InputStream.class);

        pathA = tempDir.resolve("fileA.txt");
        Path pathB = tempDir.resolve("fileB.txt");

        fileA = pathA.toString();
        fileB = pathB.toString();
        nonExistFile = tempDir.resolve("nonExistFile.txt").toString();

        Files.write(pathA, List.of(HELLO_WORLD));
        Files.write(pathB, List.of(HEY_JUNIT));
    }

    @Test
    void catFiles_NoFiles_ThrowsCutException() {
        CatException result = assertThrowsExactly(CatException.class, () -> app.catFiles(false));
        String expected = "cat: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void catFiles_EmptyFile_ThrowsCutException() {
        CatException result = assertThrowsExactly(CatException.class, () -> app.catFiles(false, new String[0]));
        String expected = "cat: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void catFiles_FileDoNotExist_PrintsErrorMessage() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, nonExistFile));
        assertEquals(ERR_NON_EXIST, result);
    }

    @Test
    void catFiles_FileGivenAsDirectory_PrintsErrorMessage() {
        Path subDir = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDir));
        String result = assertDoesNotThrow(() -> app.catFiles(false, subDir.toString()));
        String expected = "cat: 'subdirectory': This is a directory";
        assertEquals(expected, result);
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void catFiles_FileNoPermissionToRead_PrintsErrorMessage() {
        boolean isSetReadable = pathA.toFile().setReadable(false);
        if (!isSetReadable) {
            fail("Failed to set read permission to false for test");
        }

        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA));
        String expected = "cat: 'fileA.txt': Permission denied";
        assertEquals(expected, result);
    }

    @Test
    void catFiles_FileNoContent_ReturnsEmptyString() {
        assertDoesNotThrow(() -> Files.write(pathA, "".getBytes()));
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA));
        assertEmptyString(result);
    }

    @Test
    void catFiles_OneFileNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA));
        assertEquals(HELLO_WORLD, result);
    }

    @Test
    void catFiles_OneFileHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA));
        assertEquals(L1_HELLO_L2_WORLD, result);
    }

    @Test
    void catFiles_MultipleFilesNoLineNumber_ReturnsConcatenatedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA, fileB));
        assertEquals(HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT, result);
    }

    @Test
    void catFiles_MultipleFilesHasLineNumber_ReturnsLineNumberedConcatenatedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA, fileB));
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT, result);
    }

    @Test
    void catFiles_SomeFilesDoNotExistsNoLineNumber_ReturnsConcatenatedFileContentAndErrorMessage() {
        String result = assertDoesNotThrow(() -> app.catFiles(false, fileA, nonExistFile, fileB));
        assertEquals(HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE + ERR_NON_EXIST, result);
    }

    @Test
    void catFiles_SomeFilesDoNotExistsHasLineNumber_ReturnsConcatenatedFileContentAndErrorMessage() {
        String result = assertDoesNotThrow(() -> app.catFiles(true, fileA, nonExistFile, fileB));
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE + ERR_NON_EXIST, result);
    }

    @Test
    void catStdin_NullStdin_ThrowsCatException() {
        CatException result = assertThrowsExactly(CatException.class, () -> app.catStdin(false, null));
        String expected = "cat: Null Pointer Exception";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void catStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        when(assertDoesNotThrow(() -> inputStreamMock.read())).thenThrow(new IOException());
        assertThrows(CatException.class, () -> app.catStdin(false, inputStreamMock));
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void catStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catStdin(false, inputStreamMock));
        assertEquals(args, result);
    }

    @Test
    void catStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        inputStreamMock = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catStdin(true, inputStreamMock));
        assertEquals(L1_HELLO_L2_WORLD, result);
    }
}
