package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MkdirApplicationTest {

    private static final String TEMP_FILE = "file.txt";
    private static final String NON_EXISTING_FILE = "nonExistingFile.txt";
    private static final String TEMP_FILE_CONTENT = "12345";
    private MkdirApplication app;

    @TempDir
    private Path tempDir;
    private Path tempFilePath;

    @BeforeEach
    void setUp() throws IOException {
        app = new MkdirApplication();

        // Create temporary file, automatically deletes after test execution
        tempFilePath = tempDir.resolve(TEMP_FILE);
        Files.createFile(tempFilePath);
        Files.write(tempFilePath, TEMP_FILE_CONTENT.getBytes());
    }

    @Test
    void createFolder_FolderExists_NoActionTaken() {
        // Given
        assertTrue(tempFilePath.toFile().exists());
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(tempFilePath));

        // When
        app.createFolder(tempFilePath.toString());
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(tempFilePath));

        // Then
        assertTrue(tempFilePath.toFile().exists());
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void createFolder_FolderDoNotExists_CreateFolderSuccessfully() {
        Path nonExistFilePath = tempDir.resolve(NON_EXISTING_FILE);
        File nonExistFile = nonExistFilePath.toFile();
        assertDoesNotThrow(() -> app.createFolder(nonExistFile.toString()));
        assertTrue(nonExistFile.exists());
    }

    @Test
    void createFolder_FolderAndTopLevelDoNotExist_CreateFolderSuccessfully() {
        Path nonExistFilePath = tempDir.resolve("missingTopLevel/" + NON_EXISTING_FILE);
        File nonExistFile = nonExistFilePath.toFile();
        assertDoesNotThrow(() -> app.createFolder(nonExistFile.toString()));
        assertTrue(nonExistFile.exists());
    }
}
