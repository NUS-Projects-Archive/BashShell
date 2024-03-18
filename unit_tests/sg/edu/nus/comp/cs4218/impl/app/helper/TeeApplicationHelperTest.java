package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_GENERAL;
import static sg.edu.nus.comp.cs4218.test.AssertUtils.assertFileDoNotExists;
import static sg.edu.nus.comp.cs4218.test.AssertUtils.assertFileExists;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.TeeException;

public class TeeApplicationHelperTest {
    private static final String EXISTING_FILE = "existingFile.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String FILE_CONTENT_A = "A" + STRING_NEWLINE + "B" + STRING_NEWLINE + "C" + STRING_NEWLINE;
    private static final String FILE_CONTENT_B = "1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "3" + STRING_NEWLINE;
    private static final String TEE_ERROR = "tee: ";

    @TempDir
    private Path tempDir;
    private Path filePath;
    private String file;

    @BeforeEach
    void setUp() throws IOException {
        filePath = tempDir.resolve(EXISTING_FILE);
        file = filePath.toString();
        Files.createFile(filePath);
        Files.write(filePath, FILE_CONTENT_A.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void createEmptyFile_NonExistentFile_ReturnsFilePath() {
        String nonExistFile = tempDir.resolve(NON_EXISTENT_FILE).toString();
        assertFileDoNotExists(nonExistFile);
        String result = assertDoesNotThrow(() -> TeeApplicationHelper.createEmptyFile(nonExistFile));
        assertFileExists(result);
        assertEquals(tempDir.resolve(NON_EXISTENT_FILE).toString(), result);
    }

    @Test
    void createEmptyFile_ExistingFile_ThrowsTeeException() {
        TeeException result = assertThrowsExactly(TeeException.class, () -> TeeApplicationHelper.createEmptyFile(file));
        assertEquals(TEE_ERROR + ERR_GENERAL, result.getMessage());
    }

    @Test
    void writeToFile_ExistingFilePath_WritesToFile() {
        assertDoesNotThrow(() -> {
            TeeApplicationHelper.writeToFile(false, FILE_CONTENT_B, file);
            assertEquals(FILE_CONTENT_B, Files.readString(filePath));
        });
    }

    @Test
    void writeToFile_AppendExistingFile_AppendsToFile() {
        assertDoesNotThrow(() -> {
            TeeApplicationHelper.writeToFile(true, FILE_CONTENT_B, file);
            assertEquals(FILE_CONTENT_A + FILE_CONTENT_B, Files.readString(filePath));
        });
    }

    @Test
    void writeToFile_NonExistentFile_ThrowsTeeException() {
        String nonExistFile = tempDir.resolve(NON_EXISTENT_FILE).toString();
        TeeException result = assertThrowsExactly(TeeException.class, () -> TeeApplicationHelper.writeToFile(false, FILE_CONTENT_A, nonExistFile));
        assertEquals(TEE_ERROR + ERR_FILE_NOT_FOUND, result.getMessage());
    }
}
