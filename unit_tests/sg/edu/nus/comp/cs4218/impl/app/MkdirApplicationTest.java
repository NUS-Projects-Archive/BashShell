package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.test.AssertUtils.assertFileExists;

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

    @TempDir
    private Path tempDir;
    private Path filePath;
    private String file;
    private MkdirApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new MkdirApplication();

        // Create temporary file, automatically deletes after test execution
        filePath = tempDir.resolve(FILE);
        file = filePath.toString();
        Files.createFile(filePath);
        Files.write(filePath, FILE_CONTENT.getBytes());
    }

    @Test
    void createFolder_FolderExists_NoActionTaken() {
        // Given
        assertFileExists(file);
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(filePath));

        // When
        app.createFolder(filePath.toString());
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(filePath));

        // Then
        assertFileExists(file);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void createFolder_FolderDoNotExists_CreateFolderSuccessfully() {
        String nonExistFile = tempDir.resolve(NON_EXISTING_FILE).toString();
        assertDoesNotThrow(() -> app.createFolder(nonExistFile));
        assertFileExists(nonExistFile);
    }

    @Test
    void createFolder_FolderAndTopLevelDoNotExist_CreateFolderSuccessfully() {
        String nonExistFile = tempDir.resolve("missingTopLevel/" + NON_EXISTING_FILE).toString();
        assertDoesNotThrow(() -> app.createFolder(nonExistFile));
        assertFileExists(nonExistFile);
    }
}
