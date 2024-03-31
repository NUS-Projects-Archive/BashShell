package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileExists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.MkdirException;

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
    void createFolder_NullArguments_ThrowsMkdirException() {
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.createFolder());
        String expected = "mkdir: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void createFolder_FolderAsEmptyString_ThrowsMkdirException() {
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.createFolder(""));
        String expected = "mkdir: No folder names are supplied";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void createFolder_FolderExists_NoActionTaken() {
        // Given
        assertFileExists(file);
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(filePath));

        // When
        assertDoesNotThrow(() -> app.createFolder(filePath.toString()));
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
