package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.MkdirException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FOLDERS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

class MkdirApplicationTest {

    private static final String MKDIR_EX_MSG = "mkdir: ";
    private static final String EXISTING_FILE = "existingFile";
    private static final String NON_EXISTING_FILE = "nonExistingFile";
    private static final String NON_EXISTING_DIR = "nonExistingParent/nonExistingChild/nonExistingGrandchild";

    private MkdirApplication app;

    private void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    private void deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            directory.delete();
            deleteDirectory(directory.getParentFile());
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MkdirApplication();
        createFile(EXISTING_FILE);
        deleteFile(NON_EXISTING_FILE);
        deleteFile(NON_EXISTING_DIR);
    }

    @AfterEach
    void tearDown() {
        deleteFile(EXISTING_FILE);
        deleteFile(NON_EXISTING_FILE);
        deleteFile(NON_EXISTING_DIR);
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
        String[] args = {"missingTopLevel/" + NON_EXISTING_FILE};
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
        String[] args = {NON_EXISTING_FILE};
        app.run(args, null, null);
        File file = new File(NON_EXISTING_FILE);
        assertTrue(file.exists());
    }

    @Test
    void createFolder_FolderExists_ThrowsMkdirException() {
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.createFolder(EXISTING_FILE);
        });
        assertEquals(MKDIR_EX_MSG + ERR_FILE_EXISTS, result.getMessage());
    }

    @Test
    void createFolder_FolderDoNotExists_CreateFolderSuccessfully() throws MkdirException {
        File file = new File(NON_EXISTING_FILE);
        app.createFolder(NON_EXISTING_FILE);
        assertTrue(file.exists());
    }

    @Test
    void createFolder_FolderAndTopLevelDoNotExist_CreateFolderSuccessfully() throws MkdirException {
        File file = new File(NON_EXISTING_DIR);
        app.createFolder(NON_EXISTING_DIR);
        assertTrue(file.exists());
    }
}