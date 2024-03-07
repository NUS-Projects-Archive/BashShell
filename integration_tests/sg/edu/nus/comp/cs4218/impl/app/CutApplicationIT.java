package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.CutException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CutApplicationIT {

    private static final String TEMP_FILE = "file.txt";
    private static final String TEMP_CONTENT = "1234567890";

    @TempDir
    private Path tempDir;
    private Path tempFilePath;
    private CutApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new CutApplication();

        // Create temporary file, automatically deletes after test execution
        tempFilePath = tempDir.resolve(TEMP_FILE);
        Files.createFile(tempFilePath);

        // Writes content to temporary file
        Files.write(tempFilePath, TEMP_CONTENT.getBytes());
    }

    @Test
    void run_EmptyArgs_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(null, null, null));
        String expected = "cut: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_InsufficientArgs_ThrowsCutException() {
        String[] args = {"-c"};
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(args, null, null));
        String expected = "cut: Insufficient arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoStdin_ThrowsCutException() {
        String[] args = {"-c", "1-5"};
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(args, null, null));
        String expected = "cut: InputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsCutException() {
        String[] args = {"-c", "1-5", tempFilePath.toString()};
        CutException result = assertThrowsExactly(CutException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        String expected = "cut: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToReadFromInputStream_ThrowsCutException() {
        String[] args = {"-c", "1-10", tempFilePath.toString()};
        CutException result = assertThrowsExactly(CutException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockedStdin).read(any(byte[].class));
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });
        String expected = "cut: Could not read from input stream";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsCutException() {
        String[] args = {"-c", "1-10", tempFilePath.toString()};
        CutException result = assertThrowsExactly(CutException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            OutputStream mockedStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockedStdout).write(any(byte[].class));
            app.run(args, mockedStdin, mockedStdout);
        });
        String expected = "cut: Could not write to output stream";
        assertEquals(expected, result.getMessage());
    }
}
