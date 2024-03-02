package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.CutException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CutApplicationTest {

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
        String expectedMsg = "cut: Null arguments";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_InsufficientArgs_ThrowsCutException() {
        String expectedMsg = "cut: Insufficient arguments";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            String[] args = {"-c"};
            app.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_NoStdin_ThrowsCutException() {
        String expectedMsg = "cut: InputStream not provided";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            String[] args = {"-c", "1-5"};
            app.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsCutException() {
        String expectedMsg = "cut: OutputStream not provided";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            String[] args = {"-c", "1-5", tempFilePath.toString()};
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_FailsToReadFromInputStream_CutException() {
        String expectedMsg = "cut: Could not read from input stream";
        Throwable result = assertThrows(CutException.class, () -> {
            String[] args = {"-c", "1-5", tempFilePath.toString()};
            InputStream mockedStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockedStdin).read(any(byte[].class));
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });
        assertEquals(expectedMsg, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_CutException() {
        String expectedMsg = "cut: Could not write to output stream";
        Throwable result = assertThrows(CutException.class, () -> {
            String[] args = {"-c", "1-5", tempFilePath.toString()};
            InputStream mockedStdin = mock(InputStream.class);
            OutputStream mockedStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockedStdout).write(any(byte[].class));
            app.run(args, mockedStdin, mockedStdout);
        });
        assertEquals(expectedMsg, result.getMessage());
    }

    // The tests do not cover scenarios where no flag is provided, more than one flag is given,
    // or the invalidity of the range, as exceptions are expected to be thrown before reaching the cutFromFiles method.
    @Test
    void cutFromFiles_CutByChar_ReturnsCutRange() {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, range, tempFilePath.toString()));
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_CutByByte_ReturnsCutRange() {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(false, true, range, tempFilePath.toString()));
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_EmptyFile_ReturnsEmptyString() {
        // Overwrites the file content with an empty string
        assertDoesNotThrow(() -> Files.write(tempFilePath, "".getBytes()));
        String expected = "";

        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, range, tempFilePath.toString()));

        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_FileDoNotExist_ThrowsCutException() {
        String expectedMsg = "cut: No such file or directory";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
            app.cutFromFiles(true, false, null, nonExistFilePath.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromFiles_FileGivenAsDirectory_ThrowsCutException() {
        String expectedMsg = "cut: This is a directory";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            List<int[]> range = List.of(new int[]{1, 5});
            app.cutFromFiles(true, false, range, tempDir.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromFiles_FileNoPermissionToRead_ThrowsCutException() {
        String expectedMsg = "cut: Permission denied";
        boolean isReadable = tempFilePath.toFile().setReadable(false);
        if (isReadable) {
            fail("Failed to set read permission to false for test");
        }

        CutException exception = assertThrowsExactly(CutException.class, () -> {
            List<int[]> range = List.of(new int[]{1, 5});
            app.cutFromFiles(true, false, range, tempFilePath.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromStdin_CutByChar_ReturnsCutRange() {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(TEMP_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(expected, result);
    }

    @Test
    void cutFromStdin_CutByByte_ReturnsCutRange() {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(TEMP_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(expected, result);
    }

    @Test
    void cutFromStdin_EmptyStdin_ReturnsEmptyString() {
        String expected = "";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(expected, result);
    }
}
