package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileDoNotExists;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileExists;
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

    @TempDir
    private Path tempDir;
    private Path filePath;
    private String subDir;
    private String file;
    private MvApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new MvApplication();

        Path subDirPath = tempDir.resolve("subdirectory");
        assertDoesNotThrow(() -> Files.createDirectories(subDirPath));
        subDir = subDirPath.toString();

        filePath = tempDir.resolve("file");
        file = filePath.toString();
        assertDoesNotThrow(() -> Files.createFile(filePath));
    }

    @Test
    void run_NullArgs_ThrowsMvException() {
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(null, null, null));
        String expected = "mv: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_EmptyArgs_ThrowsMvException() {
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(new String[0], null, null));
        String expected = "mv: Null arguments";
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
    void run_MovesSrcFiles_MovesFile() {
        String[] args = {file, subDir};
        assertDoesNotThrow(() -> app.run(args, null, null));

        String movedFile = tempDir.resolve("subdirectory/file").toString();
        assertFileExists(movedFile); // file has moved to the new location
        assertFileDoNotExists(file); // file does not exist in the old location
    }

    @Test
    void run_DoNotExistSrcFile_ThrowsMvException() {
        String[] args = {"nonExistentFile.txt", subDir};
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(args, null, null));
        String expected = "mv: 'nonExistentFile.txt': No such file or directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_MultipleDoNotExistSrcFile_ThrowsMvException() {
        String[] args = {"nonExistentFile1.txt", "nonExistentFile2.txt", subDir};
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(args, null, null));
        String expected = "mv: 'nonExistentFile1.txt': No such file or directory" + STRING_NEWLINE +
                "mv: 'nonExistentFile2.txt': No such file or directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_SomeDoNotExistSrcFile_MovesFileAndThrowsMvException() {
        String[] args = {file, "nonExistentFile.txt", subDir};
        MvException result = assertThrowsExactly(MvException.class, () -> app.run(args, null, null));

        String movedFile = tempDir.resolve("subdirectory/file").toString();
        String expected = "mv: 'nonExistentFile.txt': No such file or directory";
        assertFileExists(movedFile); // file has moved to the new location
        assertFileDoNotExists(file); // file does not exist in the old location
        assertEquals(expected, result.getMessage());
    }
}
