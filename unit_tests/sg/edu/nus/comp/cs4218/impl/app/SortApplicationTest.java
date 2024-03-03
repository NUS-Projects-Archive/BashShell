package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.SortException;

class SortApplicationTest {

    private static final String TEMP_FILE = "file.txt";
    private SortApplication app;

    @TempDir
    private Path tempDir;
    private Path tempFilePath;
    private String tempFile;

    private String joinStringsBySystemLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new SortApplication();

        // Create temporary file, automatically deletes after test execution
        tempFilePath = tempDir.resolve(TEMP_FILE);
        tempFile = tempFilePath.toString();
        Files.createFile(tempFilePath);
    }

    @Test
    void sortFromFiles_EmptyFiles_ThrowsSortException() {
        String expectedMsg = "sort: Problem sort from file: Null arguments";
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            app.sortFromFiles(false, false, false, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void sortFromFiles_FileDoNotExist_ThrowsSortException() {
        String expectedMsg = "sort: Problem sort from file: No such file or directory";
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            app.sortFromFiles(false, false, false, nonExistFilePath.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void sortFromFiles_FileGivenAsDirectory_ThrowsSortException() {
        String expectedMsg = "sort: Problem sort from file: This is a directory";
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            app.sortFromFiles(false, false, false, tempDir.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void sortFromFiles_FileNoPermissionToRead_ThrowsSortException() {
        boolean isSetReadable = tempFilePath.toFile().setReadable(false);
        if (!isSetReadable) {
            fail("Failed to set read permission to false for test");
        }

        String expectedMsg = "sort: Problem sort from file: Permission denied";
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            app.sortFromFiles(false, false, false, tempFile);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c");
        String result = assertDoesNotThrow(() ->
                app.sortFromFiles(false, false, false, tempFile));
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsFirstWordNumberFlag_ReturnsSortedList() {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        String expected = joinStringsBySystemLineSeparator("1", "2", "10");
        String result = assertDoesNotThrow(() -> app.sortFromFiles(true, false, false, tempFile));
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsReverseOrderFlag_ReturnsReverseSortedList() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        String expected = joinStringsBySystemLineSeparator("c", "b", "a");
        String result = assertDoesNotThrow(() -> app.sortFromFiles(false, true, false, tempFile));
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsCaseIndependentFlag_ReturnsCaseIndependentSortedList() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c");
        String result = assertDoesNotThrow(() -> app.sortFromFiles(false, false, true, tempFile));
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_NoStdin_ThrowsSortException() {
        String expectedMsg = "sort: Problem sort from stdin: Null Pointer Exception";
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            app.sortFromStdin(false, false, false, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c");
        String result = assertDoesNotThrow(() -> app.sortFromStdin(false, false, false, stdin));
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsFirstWordNumberFlag_ReturnsSortedList() {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("1", "2", "10");
        String result = assertDoesNotThrow(() -> app.sortFromStdin(true, false, false, stdin));
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsReverseOrderFlag_ReturnsReverseSortedList() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("c", "b", "a");
        String result = assertDoesNotThrow(() -> app.sortFromStdin(false, true, false, stdin));
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsCaseIndependentFlag_ReturnsCaseIndependentSortedList() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c");
        String result = assertDoesNotThrow(() -> app.sortFromStdin(false, false, true, stdin));
        assertEquals(expected, result);
    }
}
