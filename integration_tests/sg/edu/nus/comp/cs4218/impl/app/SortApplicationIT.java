package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.SortException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class SortApplicationIT {

    private static final String FILE = "file.txt";
    private SortApplication app;

    @TempDir
    private Path tempDir;
    private Path filePath;
    private String file;

    private String joinStringsBySystemLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new SortApplication();

        // Create temporary file, automatically deletes after test execution
        filePath = tempDir.resolve(FILE);
        file = filePath.toString();
        Files.createFile(filePath);
    }

    @Test
    void run_NoStdout_ThrowsSortException() {
        SortException result = assertThrowsExactly(SortException.class, () -> app.run(null, null, null));
        String expected = "sort: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsSortException() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(filePath, content.getBytes()));
        String[] args = {file};
        SortException result = assertThrowsExactly(SortException.class, () -> {
            OutputStream mockedStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockedStdout).write(any(byte[].class));
            app.run(args, null, mockedStdout);
        });
        String expected = "sort: Could not write to output stream";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoFlags_WritesSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(filePath, content.getBytes()));
        String[] args = {file};
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c") + System.lineSeparator();
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsFirstWordNumberFlag_WritesSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        assertDoesNotThrow(() -> Files.write(filePath, content.getBytes()));
        String[] args = {"-n", file};
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        String expected = joinStringsBySystemLineSeparator("1", "2", "10") + System.lineSeparator();
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsReverseOrderFlag_WritesReverseSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        assertDoesNotThrow(() -> Files.write(filePath, content.getBytes()));
        String[] args = {"-r", file};
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        String expected = joinStringsBySystemLineSeparator("c", "b", "a") + System.lineSeparator();
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsCaseIndependentFlag_WritesCaseIndependentSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(filePath, content.getBytes()));
        String[] args = {"-f", file};
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c") + System.lineSeparator();
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsFirstWordNumberAndIsCaseIndependentFlag_IsFirstWordNumberTakesPrecedence() {
        String content = joinStringsBySystemLineSeparator("a", "3", "B", "2", "C", "1");
        assertDoesNotThrow(() -> Files.write(filePath, content.getBytes()));
        OutputStream stdout = new ByteArrayOutputStream();
        String[] args = {"-n", "-f", file};
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        String expected = joinStringsBySystemLineSeparator("1", "2", "3", "B", "C", "a") + System.lineSeparator();
        assertEquals(expected, stdout.toString());
    }
}
