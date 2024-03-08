package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.IOException;
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
        app = new MvApplication();
    }

    @Test
    void run_NullArgs_ThrowsMvException() {
        String expectedMsg = "mv: Missing Argument";
        MvException exception = assertThrowsExactly(MvException.class, () -> app.run(null, null, null));
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_EmptyArgsArray_ThrowsMvException() {
        String[] args = {};
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(args, null, null));
        String expected = "mv: Missing Argument";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "1", "$", "."})
    void run_InsufficientArgs_ThrowsMvException(String args) {
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(args.split("\\s+"), null, null));
        String expected = "mv: Insufficient arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_MovesSrcFiles_MovesFile(@TempDir Path tempDir) {
        Path subDir = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDir));
        Path file = tempDir.resolve("file");
        assertDoesNotThrow(() -> Files.createFile(file));

        String[] args = {file.toString(), subDir.toString()};
        assertDoesNotThrow(() -> app.run(args, null, null));

        Path movedFile = tempDir.resolve("subdirectory/file");
        assertTrue(Files.exists(movedFile)); // assert that file has moved to the new location
        assertFalse(Files.exists(file)); // assert that file does n exist in the old location
    }

    @Test
    void run_MovesMultipleDoNotExistSrcFile_ThrowsMvException(@TempDir Path tempDir) {
        Path subDir = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDir));

        String[] args = {"a", "b", subDir.toString()};
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(args, null, null));

        String expected = "mv: cannot find 'a': No such file or directory" + STRING_NEWLINE +
                "mv: cannot find 'b': No such file or directory";
        assertEquals(expected, result.getMessage());
    }
}
