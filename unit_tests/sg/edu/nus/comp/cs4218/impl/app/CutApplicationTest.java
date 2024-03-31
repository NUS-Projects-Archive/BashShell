package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.CutException;

class CutApplicationTest {

    private static final List<int[]> RANGE_ONE_TO_FIVE = List.of(new int[]{1, 5});
    private static final String FILE_ONE = "file1.txt";
    private static final String FILE_TWO = "file2.txt";
    private static final String FILE_ONE_CONTENT = "1234567890";
    private static final String FILE_TWO_CONTENT = "0987654321";
    private static final String ONE_TO_FIVE = "12345";
    private static final String ZERO_TO_SIX = "09876";
    private static final String ERR_NON_EXIST = "cut: 'nonExistFile.txt': No such file or directory";
    private static final String ERR_ONE_FLAG_ONLY = "cut: Exactly one flag (cut by character or byte) should be selected, but not both";

    private Path fileOnePath;
    private String fileOne;
    private String fileOneName;
    private String fileTwo;
    private String subDir;
    private String nonExistFile;
    private CutApplication app;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        app = new CutApplication();

        // Create temporary file, automatically deletes after test execution
        fileOnePath = createNewFile(FILE_ONE, FILE_ONE_CONTENT);
        fileOne = fileOnePath.toString();
        fileOneName = fileOnePath.toFile().getName();
        fileTwo = createNewFile(FILE_TWO, FILE_TWO_CONTENT).toString();
        subDir = createNewDirectory(tempDir, "subdirectory").toString();
        nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
    }

    // The tests do not cover scenarios where no flag is provided, more than one flag is given,
    // or the invalidity of the range, as exceptions are expected to be thrown before reaching the cutFromFiles method.
    @Test
    void cutFromFiles_BothCutOptionsFalse_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(false, false, null, new String[0])
        );
        assertEquals(ERR_ONE_FLAG_ONLY, result.getMessage());
    }

    @Test
    void cutFromFiles_BothCutOptionsTrue_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, true, null, new String[0])
        );
        assertEquals(ERR_ONE_FLAG_ONLY, result.getMessage());
    }

    @Test
    void cutFromFiles_EmptyFile_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, null, new String[0])
        );
        String expected = "cut: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromFiles_FileDoNotExist_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, null, nonExistFile)
        );
        assertEquals(ERR_NON_EXIST, result.getMessage());
    }

    @Test
    void cutFromFiles_FileGivenAsDirectory_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, subDir)
        );
        String expected = "cut: 'subdirectory': Is a directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void cutFromFiles_FileNoPermissionToRead_ThrowsCutException() {
        boolean isSetReadable = fileOnePath.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test");
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, fileOne)
        );
        String expected = String.format("cut: '%s': Could not read file", fileOneName);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromFiles_CutByChar_ReturnsCutString() {
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, fileOne));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFiles_CutByByte_ReturnsCutString() {
        String result = assertDoesNotThrow(() -> app.cutFromFiles(false, true, RANGE_ONE_TO_FIVE, fileOne));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFiles_FileNoContent_ReturnsEmptyString() {
        // Given: overwrites the file content with an empty string
        assertDoesNotThrow(() -> Files.write(fileOnePath, "".getBytes()));
        String expected = assertDoesNotThrow(() -> String.join("", Files.readAllLines(fileOnePath)));
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, fileOne));
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_MultipleFiles_ReturnsCutString() {
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, fileOne, fileTwo));
        String expected = ONE_TO_FIVE + STRING_NEWLINE + ZERO_TO_SIX;
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_SomeFilesAtTheStartDoNotExist_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, nonExistFile, fileOne, fileTwo)
        );
        assertEquals(ERR_NON_EXIST, result.getMessage());
    }

    @Test
    void cutFromFiles_SomeFilesInTheMiddleDoNotExist_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, fileOne, nonExistFile, fileTwo)
        );
        assertEquals(ERR_NON_EXIST, result.getMessage());
    }

    @Test
    void cutFromFiles_SomeFilesAtTheEndDoNotExist_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFiles(true, false, RANGE_ONE_TO_FIVE, fileOne, fileTwo, nonExistFile)
        );
        assertEquals(ERR_NON_EXIST, result.getMessage());
    }

    @Test
    void cutFromStdin_BothCutOptionsFalse_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () -> app.cutFromStdin(false, false, null, null));
        assertEquals(ERR_ONE_FLAG_ONLY, result.getMessage());
    }

    @Test
    void cutFromStdin_BothCutOptionsTrue_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () -> app.cutFromStdin(true, true, null, null));
        assertEquals(ERR_ONE_FLAG_ONLY, result.getMessage());
    }

    @Test
    void cutFromStdin_NullStdin_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () -> app.cutFromStdin(true, false, null, null));
        String expected = "cut: Null Pointer Exception";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromStdin_CutByChar_ReturnsCutString() {
        InputStream stdin = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, RANGE_ONE_TO_FIVE, stdin));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromStdin_CutByByte_ReturnsCutString() {
        InputStream stdin = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, RANGE_ONE_TO_FIVE, stdin));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromStdin_StdinNoContent_ReturnsEmptyString() {
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, RANGE_ONE_TO_FIVE, stdin));
        String expected = "";
        assertEquals(expected, result);
    }
}
