package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MkdirApplicationTest {

    private static final String FILE = "file.txt";
    private static final String NON_EXISTING_FILE = "nonExistingFile.txt";
    private static final String FILE_CONTENT = "12345";
    private MkdirApplication app;

    @TempDir
    private Path tempDir;
    private Path filePath;

    @BeforeEach
    void setUp() throws IOException {
        app = new MkdirApplication();

        // Create temporary file, automatically deletes after test execution
        filePath = tempDir.resolve(FILE);
        Files.createFile(filePath);
        Files.write(filePath, FILE_CONTENT.getBytes());
    }

    @Test
    void createFolder_FolderExists_NoActionTaken() {
        // Given
        assertTrue(filePath.toFile().exists());
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(filePath));

        // When
        app.createFolder(filePath.toString());
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(filePath));

        // Then
        assertTrue(filePath.toFile().exists());
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void createFolder_FolderDoNotExists_CreateFolderSuccessfully() {
        Path nonExistFilePath = tempDir.resolve(NON_EXISTING_FILE);
        String nonExistFile = nonExistFilePath.toFile().toString();
        assertDoesNotThrow(() -> app.createFolder(nonExistFile));
        assertTrue(Files.exists(nonExistFilePath));
    }

    @Test
    void createFolder_FolderAndTopLevelDoNotExist_CreateFolderSuccessfully() {
        Path nonExistFilePath = tempDir.resolve("missingTopLevel/" + NON_EXISTING_FILE);
        String nonExistFile = nonExistFilePath.toFile().toString();
        assertDoesNotThrow(() -> app.createFolder(nonExistFile));
        assertTrue(Files.exists(nonExistFilePath));
    }
}
