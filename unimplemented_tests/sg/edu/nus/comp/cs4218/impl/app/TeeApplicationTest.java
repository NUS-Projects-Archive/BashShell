package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

class TeeApplicationTest {
    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final String EMPTY_FILE = "empty.txt";
    private static final String CONTENT_FILE_A = "A\nB\nC\nD\nE";
    private static final String CONTENT_FILE_B = "1\n2\n3\n4\n5";
    private TeeApplication app;

    @TempDir
    private Path tempDir;
    private Path fileAPath;
    private Path fileBPath;
    private Path emptyFilePath;
    private String fileA;
    private String fileB;
    private String emptyFile;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new TeeApplication();

        fileAPath = tempDir.resolve(FILE_A);
        fileBPath = tempDir.resolve(FILE_B);
        emptyFilePath = tempDir.resolve(EMPTY_FILE);

        fileA = fileAPath.toString();
        fileB = fileBPath.toString();
        emptyFile = emptyFilePath.toString();

        Files.write(fileAPath, List.of(CONTENT_FILE_A.split("\n")));
        Files.write(fileBPath, List.of(CONTENT_FILE_B.split("\n")));
        Files.createFile(emptyFilePath);
    }

    @Test
    void run_NullOutputStream_ThrowsTeeException() {
        String expectedMsg = "tee: Null arguments";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {};
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_OnlyInvalidArgs_ThrowsTeeException() {
        String expectedMsg = "tee: illegal option -- A";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {"-A"};
            InputStream mockedStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockedStdin).read(any(byte[].class));
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });

        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_InsufficientArgs_ThrowsTeeException() {
        String expectedMsg = "tee: Insufficient arguments";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {"-a"};
            app.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsTeeException() {
        String expectedMsg = "tee: OutputStream not provided";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {"-a"};
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_ValidArgs_DoesNotThrowException() {
        assertDoesNotThrow(() -> {
            String[] args = {"-a"};
            InputStream mockedStdin = mock(InputStream.class);
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });
    }

    @Test
    void teeFromStdin_OnlyStdin_ReturnsCorrectString() {
        String expected = CONTENT_FILE_A + StringUtils.STRING_NEWLINE;
        String[] filenames = {};
        InputStream inputStream = assertDoesNotThrow(() -> IOUtils.openInputStream(fileA));
        String outputStdOut = assertDoesNotThrow(() -> app.teeFromStdin(false, inputStream, filenames));
        assertEquals(expected, outputStdOut);
    }

    @Test
    void teeFromStdin_OneFile_WritesToFile() {
        String expected = CONTENT_FILE_A + StringUtils.STRING_NEWLINE;
        InputStream inputStream = assertDoesNotThrow(() -> IOUtils.openInputStream(fileA));
        String outputStdOut = assertDoesNotThrow(() -> app.teeFromStdin(false, inputStream, emptyFile));
        String outputFile = assertDoesNotThrow(() -> Files.readString(emptyFilePath));
        assertEquals(expected, outputStdOut);
        assertEquals(expected, outputFile);
    }

    @Test
    void teeFromStdin_OneFileAndValidFlag_AppendsToFile() {
        String expected = CONTENT_FILE_B + StringUtils.STRING_NEWLINE + CONTENT_FILE_A + StringUtils.STRING_NEWLINE;
        InputStream inputStream = assertDoesNotThrow(() -> IOUtils.openInputStream(fileA));
        String outputStdOut = assertDoesNotThrow(() -> app.teeFromStdin(true, inputStream, fileB));
        String outputFile = assertDoesNotThrow(() -> Files.readString(fileBPath));
        assertEquals(expected, outputStdOut);
        assertEquals(expected, outputFile);
    }
}