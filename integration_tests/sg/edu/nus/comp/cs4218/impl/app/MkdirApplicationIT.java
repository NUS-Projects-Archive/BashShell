package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FOLDERS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.MkdirException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class MkdirApplicationIT {

    private static final String MKDIR_EX_MSG = "mkdir: ";
    private static final String TEMP_FILE = "file.txt";
    private static final String NON_EXISTING_FILE = "nonExistingFile.txt";
    private MkdirApplication app;

    @TempDir
    private Path tempDir;
    private Path tempFilePath;
    private String tempFile;

    @BeforeEach
    void setUp() throws IOException {
        app = new MkdirApplication();

        // Create temporary file, automatically deletes after test execution
        tempFilePath = tempDir.resolve(TEMP_FILE);
        tempFile = tempFilePath.toString();
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
        String[] args = {"/"};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EX_MSG + ERR_FILE_EXISTS, result.getMessage());
    }

    @Test
    void run_FolderExists_ThrowsMkdirException() {
        String[] args = {tempFile};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EX_MSG + ERR_FILE_EXISTS, result.getMessage());
    }

    @Test
    void run_MissingTopLevelAndIsCreateParent_CreateFolderSuccessfully() {
        Path missTopLevelPath = tempDir.resolve("missingTopLevel/" + TEMP_FILE);
        String[] args = {"-p", missTopLevelPath.toString()};
        assertDoesNotThrow(() -> app.run(args, null, null));
        File file = new File(missTopLevelPath.toString());
        assertTrue(file.exists());
    }

    @Test
    void run_RootDirectoryAndIsCreateParent_CreateFolderSuccessfully() {
        String[] args = {"-p", "/"};
        assertDoesNotThrow(() -> app.run(args, null, null));
        File file = new File("/");
        assertTrue(file.exists());
    }

    @Test
    void run_FolderExistsAndIsCreateParent_CreateFolderSuccessfully() {
        String[] args = {"-p", tempFile};
        assertDoesNotThrow(() -> app.run(args, null, null));
        File file = new File(tempFile);
        assertTrue(file.exists());
    }

    @Test
    void run_FolderDoNotExists_CreateFolderSuccessfully() {
        Path nonExistFilePath = tempDir.resolve(NON_EXISTING_FILE);
        String[] args = {nonExistFilePath.toString()};
        assertDoesNotThrow(() -> app.run(args, null, null));
        File file = new File(nonExistFilePath.toString());
        assertTrue(file.exists());
    }
}
