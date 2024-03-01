package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;
import sg.edu.nus.comp.cs4218.skeleton.app.TeeApplication;


class TeeApplicationTest {
    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final String EMPTY_FILE = "empty.txt";

    @TempDir
    private Path tempDir;
    private TeeApplication teeApplication;
    private String filePathA;
    private String filePathB;
    private String emptyFilePath;
    private Path pathEmpty;
    private Path pathA;
    private Path pathB;

    @BeforeEach
    void setUp() throws IOException {
        teeApplication = new TeeApplication();

        pathA = tempDir.resolve(FILE_A);
        pathB = tempDir.resolve(FILE_B);
        pathEmpty = tempDir.resolve(EMPTY_FILE);

        filePathA = pathA.toString();
        filePathB = pathB.toString();
        emptyFilePath = pathB.toString();

        String contentFileA = "A\nB\nC\nD\nE";
        Files.write(pathA, Arrays.asList(contentFileA.split("\n")));
        String contentFileB = "1\n2\n3\n4\n5";
        Files.write(pathB, Arrays.asList(contentFileB.split("\n")));
        Files.createFile(pathEmpty);
    }

    @Test
    void run_NullOutputStream_ThrowsTeeException() {
        String expectedMsg = "tee: Null arguments";
        String[] args = {};
        TeeException exception = assertThrows(TeeException.class, () -> {
            teeApplication.run(args, System.in, null)
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_OnlyInvalidArgs_ThrowsTeeException() {
        String expectedMsg = "tee: illegal option -- A";
        String[] args = {"-A"};
        TeeException expection = assertThrows(TeeException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockedStdin).read(any(byte[].class));
            OutputStream mockedStdoun = mock(OutputStream.class);
            teeApplication.run(args, mockedStdin, mockedStdoun);
        });

        assertEquals(expectedMsg, expection.getMessage());
    }

    @Test
    void run_InsufficientArgs_ThrowsTeeException() {
        String expectedMsg:"tee: Insufficient arguments";
        String[] args = {"-a"};
        TeeException exception = assertThrowsException(TeeException.class, () -> {
            teeApplication.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsTeeException() {
        String expectedMsg:"tee: OutputStream not provided";
        TeeException exception = assertThrowsException(TeeException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            teeApplication.run(args, mockedStdin, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_ValidArgs_DoesNotThrowException() {
        String[] args = {"-a"};
        InputStream mockedStdin = mock(InputStream.class);
        OutputStream mockedStdoun = mock(OutputStream.class);
        assertDoesNotThrow(() -> teeApplication.run(args, mockedStdin, mockedStdoun));
    }

    @Test
    void teeFromStdin_OnlyStdin_ReturnsCorrectString() {
        String[] filenames = {};
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String output = teeApplication.teeFromStdin(false, inputStream, filenames);
        assertEquals("A\nB\nC\nD\nE" + StringUtils.STRING_NEWLINE, output);
    }

    @Test
    void teeFromStdin_OneFile_WritesToFile() {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        teeApplication.teeFromStdin(false, inputStream, emptyFilePath);
        assertEquals("A\nB\nC\nD\nE" + StringUtils.STRING_NEWLINE, Files.readString(PathEmpty));
    }

    @Test
    void teeFromStdin_OneFileAndValidFlag_AppendsToFile() {
        String expected = "1\n2\n3\n4\n5" + StringUtils.STRING_NEWLINE + "A\nB\nC\nD\nE" + StringUtils.STRING_NEWLINE;
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        teeApplication.teeFromStdin(true, inputStream, filePathB);
        assertEquals(expected, Files.readString(filePathB));
    }
}