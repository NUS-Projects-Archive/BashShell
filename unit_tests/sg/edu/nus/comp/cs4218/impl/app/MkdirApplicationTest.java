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

    private final String MKDIR_EXCEPTION_MSG = "mkdir: ";
    private final String EXISTING_FILE = "existingFile";
    private final String NON_EXISTING_FILE = "nonExistingFile";
    private final String NON_EXISTING_FILE_AND_TOP_LEVEL = "nonExistingTopLevel/nonExistingFile";

    private MkdirApplication app;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MkdirApplication();

        File existingFile = new File(EXISTING_FILE);
        existingFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        deleteIfExists(EXISTING_FILE);
        deleteIfExists(NON_EXISTING_FILE);
        deleteIfExists(NON_EXISTING_FILE_AND_TOP_LEVEL);
    }

    private void deleteIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                // For directories, delete recursively
                File parentDirectory = file.getParentFile();
                if (parentDirectory != null) {
                    deleteIfExists(parentDirectory.getPath());
                }
            }
        }
    }

    @Test
    void run_EmptyArgs_ThrowsNullArguments() {
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(MKDIR_EXCEPTION_MSG + ERR_NULL_ARGS, result.getMessage());
    }

    @Test
    void run_NoDirectoriesArgs_ThrowsNoFolderNamesSupplied() {
        String[] args = {"-p"};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EXCEPTION_MSG + ERR_NO_FOLDERS, result.getMessage());
    }

    @Test
    void run_MissingTopLevel_ThrowsTopLevelMissing() {
        String[] args = {"missingTopLevel/" + NON_EXISTING_FILE};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EXCEPTION_MSG + ERR_TOP_LEVEL_MISSING, result.getMessage());
    }

    @Test
    void run_RootDirectory_ThrowsTopLevelMissing() {
        String[] args = {"-p", "/"};
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.run(args, null, null);
        });
        assertEquals(MKDIR_EXCEPTION_MSG + ERR_TOP_LEVEL_MISSING, result.getMessage());
    }

    @Test
    void run_FolderDoNotExists_CreateFolderSuccessfully() throws MkdirException {
        String[] args = {NON_EXISTING_FILE};
        app.run(args, null, null);
        File file = new File(NON_EXISTING_FILE);
        assertTrue(file.exists());
    }

    @Test
    void createFolder_FolderExists_ThrowsFileAlreadyExists() {
        Throwable result = assertThrows(MkdirException.class, () -> {
            app.createFolder(EXISTING_FILE);
        });
        assertEquals(MKDIR_EXCEPTION_MSG + ERR_FILE_EXISTS, result.getMessage());
    }

    @Test
    void createFolder_FolderDoNotExists_CreateFolderSuccessfully() throws MkdirException {
        File file = new File(NON_EXISTING_FILE);
        app.createFolder(NON_EXISTING_FILE);
        assertTrue(file.exists());
    }

    @Test
    void createFolder_FolderAndTopLevelDoNotExist_CreateFolderSuccessfully() throws MkdirException {
        File file = new File(NON_EXISTING_FILE_AND_TOP_LEVEL);
        app.createFolder(NON_EXISTING_FILE_AND_TOP_LEVEL);
        assertTrue(file.exists());
    }
}