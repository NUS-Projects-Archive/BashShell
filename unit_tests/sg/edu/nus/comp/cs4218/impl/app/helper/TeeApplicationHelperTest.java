package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.helper.TeeApplicationHelper.createEmptyFile;
import static sg.edu.nus.comp.cs4218.impl.app.helper.TeeApplicationHelper.writeToFile;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileDoNotExists;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileExists;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.TeeException;

public class TeeApplicationHelperTest {

    private static final String FILE_A = "fileA.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String FILE_CONTENT_A = "A" + STRING_NEWLINE + "B" + STRING_NEWLINE + "C" + STRING_NEWLINE;
    private static final String FILE_CONTENT_B = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "3" + STRING_NEWLINE;
    private Path fileAPath;
    private String fileA;

    @BeforeEach
    void setUp() {
        fileAPath = createNewFile(FILE_A, FILE_CONTENT_A);
        fileA = fileAPath.toString();
    }

    @Test
    void createEmptyFile_NonExistentFile_ReturnsFilePath(@TempDir Path tempDir) {
        String nonExistFile = tempDir.resolve(NON_EXISTENT_FILE).toString();
        assertFileDoNotExists(nonExistFile);
        String result = assertDoesNotThrow(() -> createEmptyFile(nonExistFile));
        assertFileExists(result);
        assertEquals(nonExistFile, result);
    }

    @Test
    void createEmptyFile_ExistingFile_ThrowsTeeException() {
        TeeException result = assertThrowsExactly(TeeException.class, () -> createEmptyFile(fileA));
        String expected = String.format("tee: '%s': File or directory already exists", fileAPath.getFileName());
        assertEquals(expected, result.getMessage());
    }

    @Test
    void createEmptyFile_InvalidFilePath_ThrowsTeeException() {
        TeeException result = assertThrowsExactly(TeeException.class, () -> createEmptyFile("invalid_file\0"));
        String expected = "tee: 'invalid_file\0': No such file or directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void writeToFile_NonExistentFile_ThrowsTeeException(@TempDir Path tempDir) {
        String nonExistFile = tempDir.resolve(NON_EXISTENT_FILE).toString();
        TeeException result = assertThrowsExactly(TeeException.class, () -> writeToFile(false, FILE_CONTENT_A, nonExistFile));
        String expected = String.format("tee: '%s': No such file or directory", nonExistFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void writeToFile_ExistingFilePath_WritesToFile() {
        assertDoesNotThrow(() -> writeToFile(false, FILE_CONTENT_B, fileA));
        String result = assertDoesNotThrow(() -> Files.readString(fileAPath));
        assertEquals(FILE_CONTENT_B, result);
    }

    @Test
    void writeToFile_AppendExistingFile_AppendsToFile() {
        assertDoesNotThrow(() -> writeToFile(true, FILE_CONTENT_B, fileA));
        String result = assertDoesNotThrow(() -> Files.readString(fileAPath));
        assertEquals(FILE_CONTENT_A + FILE_CONTENT_B, result);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void writeToFile_FileNoPermissionToWrite_ThrowsTeeException() {
        boolean isSetWritable = fileAPath.toFile().setWritable(false);
        assertTrue(isSetWritable, "Failed to set write permission to false for test destination file");
        TeeException result = assertThrowsExactly(TeeException.class, () -> writeToFile(true, FILE_CONTENT_B, fileA));
        String expected = "tee: IOException";
        assertEquals(expected, result.getMessage());
    }
}
