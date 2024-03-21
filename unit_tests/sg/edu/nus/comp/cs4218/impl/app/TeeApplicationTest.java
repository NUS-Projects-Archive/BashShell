package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

class TeeApplicationTest {
    private static final String TEE_EXCEPTION = "tee: ";
    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final String EMPTY_FILE = "empty.txt";
    private static final String CONTENT_FILE_A = "A" + STRING_NEWLINE + "B" + STRING_NEWLINE + "C" + STRING_NEWLINE +
            "D" + STRING_NEWLINE + "E" + STRING_NEWLINE;
    private static final String CONTENT_FILE_B = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "3" +
            STRING_NEWLINE + "4" + STRING_NEWLINE + "5" + STRING_NEWLINE;
    private TeeApplication app;
    private InputStream inputStream;
    @TempDir
    private Path tempDir;
    private Path fileBPath;
    private Path emptyFilePath;
    private String fileA;
    private String fileB;
    private String emptyFile;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new TeeApplication();

        Path fileAPath = tempDir.resolve(FILE_A);
        fileBPath = tempDir.resolve(FILE_B);
        emptyFilePath = tempDir.resolve(EMPTY_FILE);

        fileA = fileAPath.toString();
        fileB = fileBPath.toString();
        emptyFile = emptyFilePath.toString();

        Files.write(fileAPath, CONTENT_FILE_A.getBytes(StandardCharsets.UTF_8));
        Files.write(fileBPath, CONTENT_FILE_B.getBytes(StandardCharsets.UTF_8));
        Files.createFile(emptyFilePath);

        inputStream = assertDoesNotThrow(() -> IOUtils.openInputStream(fileA));
    }

    @AfterEach
    void tearDown() throws IOException {
        inputStream.close();
    }

    @Test
    void teeFromStdin_OnlyStdin_ReturnsCorrectString() {
        String[] filenames = {};
        String outputStdOut = assertDoesNotThrow(() -> app.teeFromStdin(false, inputStream, filenames));
        assertEquals(CONTENT_FILE_A, outputStdOut);
    }

    @Test
    void teeFromStdin_OneFile_WritesToFile() {
        String outputStdOut = assertDoesNotThrow(() -> app.teeFromStdin(false, inputStream, emptyFile));
        String outputFile = assertDoesNotThrow(() -> Files.readString(emptyFilePath));
        assertEquals(CONTENT_FILE_A, outputStdOut);
        assertEquals(CONTENT_FILE_A, outputFile);
    }

    @Test
    void teeFromStdin_OneFileAndValidFlag_AppendsToFile() {
        String outputStdOut = assertDoesNotThrow(() -> app.teeFromStdin(true, inputStream, fileB));
        String outputFile = assertDoesNotThrow(() -> Files.readString(fileBPath));
        String expected = CONTENT_FILE_B + CONTENT_FILE_A;
        assertEquals(CONTENT_FILE_A, outputStdOut);
        assertEquals(expected, outputFile);
    }

    @Test // picked up from evosuite automatic test generation (TeeApplication_ESTest test0)
    public void teeFromStdIn_FileIsADirectory_ReturnsFileIsADirectory() {
        String dir = tempDir.toString();
        String result = assertDoesNotThrow(() -> app.teeFromStdin(false, inputStream, dir));
        assertEquals(TEE_EXCEPTION + dir + ": Is a directory" + STRING_NEWLINE + CONTENT_FILE_A, result);
    }

    @Test // picked up from evosuite automatic test generation (TeeApplication_ESTest test3)
    public void teeFromStdIn_NullFile_ThrowsNullArgsException() {
        TeeException result = assertThrowsExactly(TeeException.class, () -> app.teeFromStdin(false, inputStream, null));
        assertEquals(TEE_EXCEPTION + ERR_NULL_ARGS, result.getMessage());
    }
}
