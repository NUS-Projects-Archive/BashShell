package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.MvException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class MvApplicationIT {

    private MvApplication app;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MvApplication();
    }

    @Test
    void run_NullArgs_ThrowsMvException() {
        String expectedMsg = "mv: Missing Argument";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_EmptyArgsArray_ThrowsMvException() {
        String expectedMsg = "mv: Missing Argument";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            String[] args = {};
            app.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "1", "$", "."})
    void run_InsufficientArgs_ThrowsMvException(String args) {
        String expectedMsg = "mv: Insufficient arguments";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.run(args.split("\\s+"), null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_MovesSrcFiles_DoNotWriteToStdout(@TempDir Path tempDir) {
        Path subDir = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDir));
        Path file = tempDir.resolve("file");
        assertDoesNotThrow(() -> Files.createFile(file));

        String[] args = {file.toString(), subDir.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, null, stdout));

        String expected = "";
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_MovesMultipleDoNotExistSrcFile_WritesErrorToStdout(@TempDir Path tempDir) {
        Path subDir = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDir));

        String[] args = {"a", "b", subDir.toString()};
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, null, stdout));

        String expected = "mv: Problem move to folder: 'a': No such file or directory" + System.lineSeparator() +
                "mv: Problem move to folder: 'b': No such file or directory" + System.lineSeparator();
        assertEquals(expected, stdout.toString());
    }
}
