package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.SortException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.exception.SortException.PROB_SORT_FILE;
import static sg.edu.nus.comp.cs4218.exception.SortException.PROB_SORT_STDIN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;

class SortApplicationTest {

    private static final String SORT_EX_MSG = "sort: ";
    private static final String TEMP_FILE = "file.txt";
    private SortApplication app;

    @TempDir
    private Path tempDir;
    private Path tempFilePath;

    private String joinStringsBySystemLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    @BeforeEach
    void setUp() throws IOException {
        this.app = new SortApplication();

        // Create temporary file
        tempFilePath = tempDir.resolve(TEMP_FILE); // automatically deletes after test execution
        Files.createFile(tempFilePath);
    }

    @Test
    void run_NoStdout_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(SORT_EX_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsSortException() throws IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        Files.write(tempFilePath, content.getBytes());
        String[] args = {tempFilePath.toString()};
        OutputStream mockedStdout = mock(OutputStream.class);
        doThrow(new IOException()).when(mockedStdout).write(any(byte[].class));
        Throwable result = assertThrows(SortException.class, () -> {
            app.run(args, null, mockedStdout);
        });
        assertEquals(SORT_EX_MSG + ERR_WRITE_STREAM, result.getMessage());
    }

    @Test
    void run_NoFlags_WritesSortedListToStdout() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        Files.write(tempFilePath, content.getBytes());
        String[] args = {tempFilePath.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c") + System.lineSeparator();
        app.run(args, null, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsFirstWordNumberFlag_WritesSortedListToStdout() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        Files.write(tempFilePath, content.getBytes());
        String[] args = {"-n", tempFilePath.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("1", "2", "10") + System.lineSeparator();
        app.run(args, null, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsReverseOrderFlag_WritesReverseSortedListToStdout() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        Files.write(tempFilePath, content.getBytes());
        String[] args = {"-r", tempFilePath.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("c", "b", "a") + System.lineSeparator();
        app.run(args, null, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsCaseIndependentFlag_WritesCaseIndependentSortedListToStdout() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        Files.write(tempFilePath, content.getBytes());
        String[] args = {"-f", tempFilePath.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c") + System.lineSeparator();
        app.run(args, null, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsFirstWordNumberAndIsCaseIndependentFlag_IsFirstWordNumberTakesPrecedence()
            throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "3", "B", "2", "C", "1");
        Files.write(tempFilePath, content.getBytes());
        String[] args = {"-n", "-f", tempFilePath.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("1", "2", "3", "B", "C", "a")
                + System.lineSeparator();
        app.run(args, null, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    void sortFromFiles_EmptyFiles_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(false, false, false, null);
        });
        assertEquals(SORT_EX_MSG + PROB_SORT_FILE + ERR_NULL_ARGS, result.getMessage());
    }

    @Test
    void sortFromFiles_FileDoNotExist_ThrowsSortException() {
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(false, false, false, nonExistFilePath.toString());
        });
        assertEquals(SORT_EX_MSG + PROB_SORT_FILE + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    void sortFromFiles_FileGivenAsDirectory_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(false, false, false, tempDir.toString());
        });
        assertEquals(SORT_EX_MSG + PROB_SORT_FILE + ERR_IS_DIR, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void sortFromFiles_FileNoPermissionToRead_ThrowsSortException() throws IOException {
        Files.setPosixFilePermissions(tempFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(false, false, false, TEMP_FILE);
        });
        assertEquals(SORT_EX_MSG + PROB_SORT_FILE + ERR_NO_PERM, result.getMessage());
    }

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        Files.write(tempFilePath, content.getBytes());
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c");
        String result = app.sortFromFiles(false, false, false, tempFilePath.toString());
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsFirstWordNumberFlag_ReturnsSortedList() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        Files.write(tempFilePath, content.getBytes());
        String expected = joinStringsBySystemLineSeparator("1", "2", "10");
        String result = app.sortFromFiles(true, false, false, tempFilePath.toString());
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsReverseOrderFlag_ReturnsReverseSortedList() throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        Files.write(tempFilePath, content.getBytes());
        String expected = joinStringsBySystemLineSeparator("c", "b", "a");
        String result = app.sortFromFiles(false, true, false, tempFilePath.toString());
        assertEquals(expected, result);
    }

    @Test
    void sortFromFiles_IsCaseIndependentFlag_ReturnsCaseIndependentSortedList()
            throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        Files.write(tempFilePath, content.getBytes());
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c");
        String result = app.sortFromFiles(false, false, true, tempFilePath.toString());
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_NoStdin_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromStdin(false, false, false, null);
        });
        assertEquals(SORT_EX_MSG + PROB_SORT_STDIN + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() throws SortException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c");
        String result = app.sortFromStdin(false, false, false, stdin);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsFirstWordNumberFlag_ReturnsSortedList() throws SortException {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("1", "2", "10");
        String result = app.sortFromStdin(true, false, false, stdin);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsReverseOrderFlag_ReturnsReverseSortedList() throws SortException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("c", "b", "a");
        String result = app.sortFromStdin(false, true, false, stdin);
        assertEquals(expected, result);
    }

    @Test
    void sortFromStdin_IsCaseIndependentFlag_ReturnsCaseIndependentSortedList() throws SortException {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        InputStream stdin = new ByteArrayInputStream(content.getBytes());
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c");
        String result = app.sortFromStdin(false, false, true, stdin);
        assertEquals(expected, result);
    }
}