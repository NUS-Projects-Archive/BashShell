package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.SortException;

class SortApplicationTest {

    private static final String FILE = "file.txt";
    private static final String BIG_A = "A";
    private static final String BIG_B = "B";
    private static final String SMALL_A = "a";
    private static final String SMALL_B = "b";
    private static final String SMALL_O = "o";
    private static final String PLUS = "+";
    private static final String MINUS = "-";

    private static final String[] CONTENT = {"5", BIG_A, "2", BIG_B, "10", SMALL_O, "1", SMALL_A, "3", PLUS, SMALL_B, MINUS};
    private static final String[] OUT_NO_FLAGS = {PLUS, MINUS, "1", "10", "2", "3", "5", BIG_A, BIG_B, SMALL_A, SMALL_B, SMALL_O};
    private static final String[] OUT_FIRST_NUM = {PLUS, MINUS, BIG_A, BIG_B, SMALL_A, SMALL_B, SMALL_O, "1", "2", "3", "5", "10"};
    private static final String[] OUT_REV_ORDER = {SMALL_O, SMALL_B, SMALL_A, BIG_B, BIG_A, "5", "3", "2", "10", "1", MINUS, PLUS};
    private static final String[] OUT_CASE_IGNORE = {PLUS, MINUS, "1", "10", "2", "3", "5", BIG_A, SMALL_A, BIG_B, SMALL_B, SMALL_O};

    private SortApplication app;
    private InputStream stdin;
    private Path filePath;
    private String file;
    private String fileName;

    @BeforeEach
    void setUp() throws IOException {
        app = new SortApplication();

        // Create temporary file, automatically deletes after test execution
        String content = joinStringsByNewline(CONTENT);
        stdin = new ByteArrayInputStream(content.getBytes());
        filePath = createNewFile(FILE, content);
        file = filePath.toString();
        fileName = filePath.toFile().getName();
    }

    @Test
    void sortFromFiles_EmptyFile_ThrowsSortException() {
        SortException result = assertThrowsExactly(SortException.class, () ->
                app.sortFromFiles(false, false, false, new String[0])
        );
        String expected = "sort: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void sortFromFiles_FileDoNotExist_ThrowsSortException(@TempDir Path tempDir) {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        SortException result = assertThrowsExactly(SortException.class, () ->
                app.sortFromFiles(false, false, false, nonExistFile)
        );
        String expected = "sort: 'nonExistFile.txt': No such file or directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void sortFromFiles_FileGivenAsDirectory_ThrowsSortException(@TempDir Path tempDir) {
        String directory = createNewDirectory(tempDir, "directory").toString();
        SortException result = assertThrowsExactly(SortException.class, () ->
                app.sortFromFiles(false, false, false, directory)
        );
        String expected = "sort: 'directory': Is a directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void sortFromFiles_FileNoPermissionToRead_ThrowsSortException() {
        boolean isSetReadable = filePath.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test");
        SortException result = assertThrowsExactly(SortException.class, () ->
                app.sortFromFiles(false, false, false, file)
        );
        String expected = String.format("sort: '%s': Could not read file", fileName);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromFiles(false, false, false, file));
        String expected = joinStringsByNewline(OUT_NO_FLAGS);
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsFirstWordNumberFlag_ReturnsSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromFiles(true, false, false, file));
        String expected = joinStringsByNewline(OUT_FIRST_NUM);
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsReverseOrderFlag_ReturnsReverseSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromFiles(false, true, false, file));
        String expected = joinStringsByNewline(OUT_REV_ORDER);
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsCaseIndependentFlag_ReturnsCaseIndependentSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromFiles(false, false, true, file));
        String expected = joinStringsByNewline(OUT_CASE_IGNORE);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_NoStdin_ThrowsSortException() {
        SortException result = assertThrowsExactly(SortException.class, () ->
                app.sortFromStdin(false, false, false, null)
        );
        String expected = "sort: Null Pointer Exception";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromStdin(false, false, false, stdin));
        String expected = joinStringsByNewline(OUT_NO_FLAGS);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsFirstWordNumberFlag_ReturnsSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromStdin(true, false, false, stdin));
        String expected = joinStringsByNewline(OUT_FIRST_NUM);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsReverseOrderFlag_ReturnsReverseSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromStdin(false, true, false, stdin));
        String expected = joinStringsByNewline(OUT_REV_ORDER);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsCaseIndependentFlag_ReturnsCaseIndependentSortedList() {
        String result = assertDoesNotThrow(() -> app.sortFromStdin(false, false, true, stdin));
        String expected = joinStringsByNewline(OUT_CASE_IGNORE);
        assertEquals(expected, result);
    }
}
