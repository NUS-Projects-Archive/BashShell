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
        this.app = new SortApplication();

        // Create temporary file, automatically deletes after test execution
        tempFilePath = tempDir.resolve(TEMP_FILE);
        tempFile = tempFilePath.toString();
        Files.createFile(tempFilePath);
    }

    @Test
    void run_NoStdout_ThrowsSortException() {
        String expectedMsg = "sort: Null Pointer Exception";
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsSortException() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        String expectedMsg = "sort: Could not write to output stream";
        SortException exception = assertThrowsExactly(SortException.class, () -> {
            String[] args = {tempFile};
            OutputStream mockedStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockedStdout).write(any(byte[].class));
            app.run(args, null, mockedStdout);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_NoFlags_WritesSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("A", "a", "b", "c") + System.lineSeparator();
        String[] args = {tempFile};
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsFirstWordNumberFlag_WritesSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("10", "1", "2");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("1", "2", "10") + System.lineSeparator();
        String[] args = {"-n", tempFile};
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsReverseOrderFlag_WritesReverseSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("c", "b", "a") + System.lineSeparator();
        String[] args = {"-r", tempFile};
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsCaseIndependentFlag_WritesCaseIndependentSortedListToStdout() {
        String content = joinStringsBySystemLineSeparator("a", "c", "b", "A");
        assertDoesNotThrow(() -> Files.write(tempFilePath, content.getBytes()));
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("a", "A", "b", "c") + System.lineSeparator();
        String[] args = {"-f", tempFile};
        assertDoesNotThrow(() -> app.run(args, null, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_IsFirstWordNumberAndIsCaseIndependentFlag_IsFirstWordNumberTakesPrecedence()
            throws SortException, IOException {
        String content = joinStringsBySystemLineSeparator("a", "3", "B", "2", "C", "1");
        Files.write(tempFilePath, content.getBytes());
        OutputStream stdout = new ByteArrayOutputStream();
        String expected = joinStringsBySystemLineSeparator("1", "2", "3", "B", "C", "a")
                + System.lineSeparator();
        String[] args = {"-n", "-f", tempFile};
        app.run(args, null, stdout);
        assertEquals(expected, stdout.toString());
    }
}
