package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.MkdirException;

class MkdirApplicationTest {

    private static final String MKDIR_EX_MSG = "mkdir: ";
    private static final String TEMP_FILE = "file.txt";
    private static final String NON_EXISTING_FILE = "nonExistingFile.txt";
    private MkdirApplication app;

    @TempDir
    private Path tempDir;
    private Path tempFilePath;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MkdirApplication();

        // Create temporary file
        tempFilePath = tempDir.resolve(TEMP_FILE); // automatically deletes after test execution
        Files.createFile(tempFilePath);
    }

    @Test
    void createFolder_FolderExists_ThrowsMkdirException() {
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.createFolder(tempFilePath.toString());
        });
        assertEquals(MKDIR_EX_MSG + ERR_FILE_EXISTS, result.getMessage());
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
