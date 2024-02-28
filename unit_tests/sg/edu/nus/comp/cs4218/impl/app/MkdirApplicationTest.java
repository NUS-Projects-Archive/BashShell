package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.MkdirException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FOLDERS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

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
    void run_EmptyArgs_ThrowsMkdirException() {
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(MKDIR_EX_MSG + ERR_NULL_ARGS, result.getMessage());
    }

    @Test
    void run_NoDirectoriesArgs_ThrowsMkdirException() {
        String[] args = {"-p"};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EX_MSG + ERR_NO_FOLDERS, result.getMessage());
    }

    @Test
    void run_MissingTopLevel_ThrowsMkdirException() {
        Path missTopLevelPath = tempDir.resolve("missingTopLevel/" + TEMP_FILE);
        String[] args = {missTopLevelPath.toString()};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EX_MSG + ERR_TOP_LEVEL_MISSING, result.getMessage());
    }

    @Test
    void run_RootDirectory_ThrowsMkdirException() {
        String[] args = {"-p", "/"};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EX_MSG + ERR_TOP_LEVEL_MISSING, result.getMessage());
    }

    @Test
    void run_FolderDoNotExists_CreateFolderSuccessfully() throws MkdirException {
        Path nonExistFilePath = tempDir.resolve("missingTopLevel/" + NON_EXISTING_FILE);
        String[] args = {"-p", nonExistFilePath.toString()};
        app.run(args, null, null);
        File file = new File(nonExistFilePath.toString());
        assertTrue(file.exists());
    }

    @Test
    void createFolder_FolderExists_ThrowsMkdirException() {
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.createFolder(tempFilePath.toString());
        });
        assertEquals(MKDIR_EX_MSG + ERR_FILE_EXISTS, result.getMessage());
    }

    @Test
    void createFolder_FolderDoNotExists_CreateFolderSuccessfully() throws MkdirException {
        Path nonExistFilePath = tempDir.resolve(NON_EXISTING_FILE);
        File nonExistFile = nonExistFilePath.toFile();
        app.createFolder(nonExistFile.toString());
        assertTrue(nonExistFile.exists());
    }

    @Test
    void createFolder_FolderAndTopLevelDoNotExist_CreateFolderSuccessfully() throws MkdirException {
        Path nonExistFilePath = tempDir.resolve("missingTopLevel/" + NON_EXISTING_FILE);
        File nonExistFile = nonExistFilePath.toFile();
        app.createFolder(nonExistFile.toString());
        assertTrue(nonExistFile.exists());
    }
}